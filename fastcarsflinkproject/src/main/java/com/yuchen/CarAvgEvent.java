package com.yuchen;
/**
 * Created by YuChen on 2023-02-08.
 * 计算结果流数据类型
 * id 速度 加速度 时间戳
 */

public class CarAvgEvent {

    public String carId;           	// 车辆id
    public double avgSpeed;      	// 平均速度
    public String start;  			// 计算平均值的时间范围下限
    public String end;				// 计算平均值的时间范围上限

    public CarAvgEvent() {
    }

    // 构造函数
    public CarAvgEvent(String carId, double avgSpeed, String start, String end) {
        this.carId = carId;
        this.avgSpeed = avgSpeed;
        this.start = start;
        this.end = end;
    }
    // toString
    @Override
    public String toString() {
        return "CarAvgEvent{" +
                "carId='" + carId + '\'' +
                ", avgSpeed=" + avgSpeed +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                '}';
    }
}