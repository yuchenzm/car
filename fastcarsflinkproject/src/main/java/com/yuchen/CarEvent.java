package com.yuchen;
/**
 * Created by yuchen on 2023-02-08.
 * 源数据流类型
 */
public class CarEvent {

    public String carId;           	// 车辆id
    public int speed;      			// 速度
    public double acceleration;  	// 加速度
    public long timestamp;			// 时间戳

    public CarEvent() {
    }
// 构造函数
    public CarEvent(String carId, int speed, double acceleration, long timestamp) {
        this.carId = carId;
        this.speed = speed;
        this.acceleration = acceleration;
        this.timestamp = timestamp;
    }
// toString
    @Override
    public String toString() {
        return "CarEvent{" +
                "carId='" + carId + '\'' +
                ", speed=" + speed +
                ", acceleration=" + acceleration +
                ", timestamp=" + timestamp +
                '}';
    }
}
