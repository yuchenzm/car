const totalCars = 10;
let overspeedCars = 0;
const carSpeeds = {};
const fluctuationData = {};
const speedHistory = [];
const maxHistoryLength = 100; // 最大历史记录数量

// 定义固定的颜色数组
const carColors = [
    '#FF6B6B', // 红色
    '#4ECDC4', // 青色
    '#45B7D1', // 蓝色
    '#96CEB4', // 绿色
    '#FFEEAD', // 黄色
    '#D4A5A5', // 粉色
    '#9370DB', // 紫色
    '#20B2AA', // 青绿
    '#FF8C00', // 橙色
    '#BA55D3'  // 紫罗兰
];

// 初始化仪表盘
function initCharts() {
    const chartsContainer = $(".charts-container table");
    chartsContainer.empty(); // 确保容器清空
    let row;
    for (let i = 0; i < totalCars; i++) {
        if (i % 3 === 0) {
            row = $("<tr></tr>");
            chartsContainer.append(row);
        }
        const carId = `car${i}`;
        const chartCell = $(`<td><div id="${carId}" class="gauge"></div><div id="msg_${carId}" class="gauge-msg"></div></td>`);
        row.append(chartCell);

        const chart = echarts.init(document.getElementById(carId));
        chart.setOption(getChartOption(carId, 0));
        window[carId] = chart; // 保存图表实例
        carSpeeds[carId] = 0; // 初始化车速
    }
}

// 获取仪表盘选项
function getChartOption(carId, value) {
    return {
        series: [
            {
                type: "gauge",
                min: 0,
                max: 120,
                splitNumber: 12,
                axisLine: { lineStyle: { color: [[0.8, "#00ff99"], [1, "#ff3333"]], width: 10 } },
                detail: { formatter: "{value} km/h", fontSize: 16 },
                data: [{ value, name: carId }],
            },
        ],
    };
}

// 修改初始化波动图表的配置
function initFluctuationChart() {
    const fluctuationChart = echarts.init(document.getElementById("fluctuationContainer"));
    fluctuationChart.setOption({
        title: {
            text: "车辆超速波动统计",
            left: "center",
            top: "5%",
            textStyle: {
                color: "#ffffff",
                fontSize: 18,
            },
        },
        tooltip: {
            trigger: "axis",
            formatter: function(params) {
                let result = params[0].axisValue + '<br/>';
                params.forEach(param => {
                    // 显示车辆编号和具体速度
                    result += `${param.seriesName}：${param.value}km/h<br/>`;
                });
                return result;
            }
        },
        legend: {
            data: [],
            top: "10%",
            textStyle: {
                color: "#ffffff",
            },
            formatter: function(name) {
                return '车辆-' + name.replace('car', '');  // 将car0改为车辆-0
            }
        },
        grid: {
            top: "20%", // 确保图表内容距离顶部足够远
            left: "10%",
            right: "10%",
            bottom: "15%",
        },
        xAxis: {
            type: "category",
            boundaryGap: false,
            data: [],
            axisLabel: {
                color: "#ffffff",
            },
        },
        yAxis: {
            type: "value",
            min: 80,  // 设置最小值为80
            max: 120, // 设置最大值为120，突出显示超速区域
            splitLine: {
                show: true,
                lineStyle: {
                    color: 'rgba(255,255,255,0.1)'
                }
            },
            axisLabel: {
                color: "#ffffff",
            },
            axisLine: {
                lineStyle: {
                    color: '#ffffff'
                }
            },
            splitArea: {
                show: true,
                areaStyle: {
                    color: ['rgba(255,51,51,0.1)', 'rgba(255,51,51,0.2)']
                }
            }
        },
        series: [],
        visualMap: {
            show: false,
            pieces: [{
                gt: 100,
                lte: 120,
                color: '#ff3333'
            }, {
                gt: 80,
                lte: 100,
                color: '#00ff99'
            }]
        }
    });

    window.fluctuationChart = fluctuationChart;
}

// 修改更新波动统计数据的方法
function updateFluctuation(carId, speed) {
    if (!fluctuationData[carId]) fluctuationData[carId] = { timestamps: [], speeds: [] };

    const now = new Date().toLocaleTimeString();
    fluctuationData[carId].timestamps.push(now);
    fluctuationData[carId].speeds.push(speed);

    // 限制数据点数量，保持最新的30个数据点
    if (fluctuationData[carId].timestamps.length > 30) {
        fluctuationData[carId].timestamps.shift();
        fluctuationData[carId].speeds.shift();
    }

    const chart = window.fluctuationChart;
    const series = Object.keys(fluctuationData).map((key, index) => ({
        name: key,
        type: "line",
        data: fluctuationData[key].speeds,
        smooth: true,
        lineStyle: {
            width: 2,
            color: carColors[index % carColors.length]  // 使用固定颜色
        },
        symbol: 'circle',
        symbolSize: function(val) {
            return val > 100 ? 8 : 4;
        },
        itemStyle: {
            color: function(params) {
                if (params.value > 100) {
                    return '#ff3333';
                }
                return carColors[index % carColors.length];  // 正常速度使用对应的车辆颜色
            }
        },
        emphasis: {
            focus: 'series',
            lineStyle: {
                width: 4
            }
        }
    }));

    chart.setOption({
        legend: {
            data: Object.keys(fluctuationData),
            formatter: function(name) {
                return '车辆-' + name.replace('car', '');
            }
        },
        xAxis: { data: fluctuationData[carId].timestamps },
        series
    });
}

// 更新单个仪表盘
function updateChart(data) {
    const { carId, avgSpeed, start, end } = data;
    const chart = window[carId];
    if (chart) {
        chart.setOption({
            series: [{ data: [{ value: avgSpeed, name: carId }] }],
        });

        $(`#msg_${carId}`).text(avgSpeed > 100 ? `${start} - ${end} 超速` : "正常行驶").toggleClass("overspeed", avgSpeed > 100);

        carSpeeds[carId] = avgSpeed;

        updateStats();
        updateFluctuation(carId, avgSpeed);

        // 添加到历史记录
        addToHistory({
            time: new Date().toLocaleString(),
            carId: carId,
            speed: avgSpeed,
            status: avgSpeed > 100 ? 'overspeed' : 'normal'
        });
    }
}

// 更新统计信息
function updateStats() {
    overspeedCars = Object.values(carSpeeds).filter((speed) => speed > 100).length;
    const overspeedPercentage = ((overspeedCars / totalCars) * 100).toFixed(2);

    $("#overspeedCars").text(overspeedCars);
    $("#overspeedPercentage").text(`${overspeedPercentage}%`);
}

// 初始化 WebSocket
function initWebSocket() {
    const ws = new WebSocket("ws://localhost:8080/dashboard");
    ws.onopen = () => console.log("WebSocket connected!");
    ws.onclose = () => console.log("WebSocket disconnected!");
    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);
        updateChart(data);
    };
    ws.onerror = (error) => console.error("WebSocket error:", error);
}

// 添加历史记录函数
function addToHistory(record) {
    speedHistory.unshift(record); // 在开头添加新记录
    if (speedHistory.length > maxHistoryLength) {
        speedHistory.pop(); // 移除最老的记录
    }
    updateHistoryTable();
}

// 更新历史记录表格
function updateHistoryTable() {
    const tbody = $("#historyTableBody");
    const carFilter = $("#carFilter").val();
    const speedFilter = $("#speedFilter").val();

    // 过滤记录
    const filteredHistory = speedHistory.filter(record => {
        if (carFilter !== 'all' && record.carId !== carFilter) return false;
        if (speedFilter === 'overspeed' && record.status !== 'overspeed') return false;
        if (speedFilter === 'normal' && record.status !== 'normal') return false;
        return true;
    });

    // 更新表格内容
    tbody.empty();
    filteredHistory.forEach(record => {
        tbody.append(`
            <tr>
                <td>${record.time}</td>
                <td>${record.carId}</td>
                <td>${record.speed}</td>
                <td class="status-${record.status}">
                    ${record.status === 'overspeed' ? '超速' : '正常'}
                </td>
            </tr>
        `);
    });
}

// 页面加载后初始化
$(document).ready(() => {
    $(".sidebar nav ul li").on("click", function () {
        const page = $(this).data("page");
        $(".content-section").addClass("hidden");
        $(`#${page}`).removeClass("hidden");
        $(".sidebar nav ul li").removeClass("active");
        $(this).addClass("active");

        if (page === "dashboard") {
            Object.keys(carSpeeds).forEach((carId) => {
                const chart = window[carId];
                if (chart) chart.resize();
            });
        } else if (page === "fluctuation") {
            const chart = window.fluctuationChart;
            if (chart) chart.resize();
        }
    });

    // 处理登录表单提交
    $("#loginForm").on("submit", function(e) {
        e.preventDefault();
        const username = $("#username").val();
        const password = $("#password").val();

        // 这里使用简单的判断，实际项目中应该与后端进行验证
        if (username === "admin" && password === "admin123") {
            $("#loginPage").hide();
            $("#mainPanel").show();
            initCharts();
            initFluctuationChart();
            initWebSocket();
        } else {
            alert("用户名或密码错误！");
        }
    });

    // 注释掉或删除原有的直接初始化代码
    // initCharts();
    // initFluctuationChart();
    // initWebSocket();

    // 初始化车辆过滤器选项
    const carFilter = $("#carFilter");
    for (let i = 0; i < totalCars; i++) {
        carFilter.append(`<option value="car${i}">车辆-${i}</option>`);
    }

    // 添加过滤器变化事件监听
    $("#carFilter, #speedFilter").on("change", updateHistoryTable);
});