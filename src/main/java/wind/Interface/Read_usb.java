package wind.Interface;

import gnu.io.SerialPort;
import wind.Until.KafkaUtil;

import java.io.IOException;

public interface Read_usb {

    /**
     * 打开串口
     *
     * @param portName
     *            端口名称
     * @param baudrate
     *            波特率
     * @return 串口对象
     * @throws PortInUseException
     *             串口已被占用
     */


    public SerialPort openPort();

    /**
     * 关闭串口
     *
     * @param serialport
     *            待关闭的串口对象
     */
    public  void closePort(SerialPort serialPort);


    /**
     * 从串口读取数据
     *
     * @param serialPort
     *            当前已建立连接的SerialPort对象
     * @return 读取到的数据
     */
    /**
     * 从串口读取数据
     *
     * @param serialPort
     *            当前已建立连接的SerialPort对象
     * @return 读取到的数据
     */
    public  byte[] readFromPort(SerialPort serialPort) throws IOException;
    /**
     * 添加监听器
     *
     * @param port
     *            串口对象
     * @param listener
     *            串口存在有效数据监听
     */
    /**
     * 添加监听器
     *
     * @count 多少条提交一次
     *
     * @param
     *
     */
    public void addListener(SerialPort serialPort, KafkaUtil ka1,int count);

}
