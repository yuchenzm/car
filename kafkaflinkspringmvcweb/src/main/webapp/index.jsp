<!--
* @Author: error: error: git config user.name & please set dead value or install git && error: git config user.email & please set dead value or install git & please set dead value or install git
* @Date: 2024-04-15 15:19:46
* @LastEditors: error: error: git config user.name & please set dead value or install git && error: git config user.email & please set dead value or install git & please set dead value or install git
* @LastEditTime: 2025-01-21 14:47:40
* @FilePath: /kafkaflinkspringmvcweb/src/main/webapp/index.jsp
* @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>物流公司车辆超速监测</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/tech_style.css">
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery-3.1.1.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/echarts.min.js"></script>
</head>
<body>
<!-- 登录页面 -->
<div id="loginPage" class="login-container">
    <div class="login-box">
        <h2>车辆监控系统登录</h2>
        <form id="loginForm">
            <div class="input-group">
                <input type="text" id="username" placeholder="用户名" required>
            </div>
            <div class="input-group">
                <input type="password" id="password" placeholder="密码" required>
            </div>
            <button type="submit" class="login-btn">登录</button>
        </form>
    </div>
</div>

<!-- 主面板（默认隐藏） -->
<div id="mainPanel" class="admin-dashboard" style="display: none;">
    <!-- 侧边导航栏 -->
    <aside class="sidebar">
        <h2>车辆监控后台</h2>
        <nav>
            <ul>
                <li data-page="realtime" class="active">实时监控</li>
                <li data-page="dashboard">车辆仪表盘</li>
                <li data-page="fluctuation">超速波动统计</li>
            </ul>
        </nav>
    </aside>

    <!-- 主内容区域 -->
    <main class="main-content">
        <!-- 实时监控 -->
        <section id="realtime" class="content-section">
            <header>
                <h1>🚛 实时监控</h1>
            </header>
            <div class="stats">
                <div class="stat-box">
                    <h2>车辆总数</h2>
                    <p id="totalCars">10</p>
                </div>
                <div class="stat-box">
                    <h2>超速车辆</h2>
                    <p id="overspeedCars">0</p>
                </div>
                <div class="stat-box">
                    <h2>超速百分比</h2>
                    <p id="overspeedPercentage">0%</p>
                </div>
            </div>

            <!-- 添加历史记录区域 -->
            <div class="history-section">
                <h2>历史记录</h2>
                <div class="history-filters">
                    <select id="carFilter">
                        <option value="all">所有车辆</option>
                    </select>
                    <select id="speedFilter">
                        <option value="all">所有记录</option>
                        <option value="overspeed">仅超速记录</option>
                        <option value="normal">仅正常记录</option>
                    </select>
                </div>
                <div class="history-table-container">
                    <table class="history-table">
                        <thead>
                        <tr>
                            <th>时间</th>
                            <th>车辆编号</th>
                            <th>速度(km/h)</th>
                            <th>状态</th>
                        </tr>
                        </thead>
                        <tbody id="historyTableBody">
                        <!-- 历史记录将动态插入这里 -->
                        </tbody>
                    </table>
                </div>
            </div>
        </section>

        <!-- 仪表盘 -->
        <section id="dashboard" class="content-section hidden">
            <header>
                <h1>🚛 仪表盘</h1>
            </header>
            <div class="charts-container">
                <table>
                    <!-- 动态生成仪表盘 -->
                </table>
            </div>
        </section>

        <!-- 超速波动统计 -->
        <section id="fluctuation" class="content-section hidden">
            <header>
                <h1>🚛 超速波动统计</h1>
            </header>
            <div id="fluctuationContainer" class="chart-box"></div>
        </section>
    </main>
</div>

<script src="<%=request.getContextPath()%>/js/admin-scripts.js"></script>
</body>
</html>