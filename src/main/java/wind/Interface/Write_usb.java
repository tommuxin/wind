package wind.Interface;

import gnu.io.SerialPort;

import java.util.ArrayList;

public interface Write_usb {

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
     * 往串口发送数据
     *
     * @param serialPort
     *            串口对象
     * @param order
     *            待发送数据
     */
    public  void sendToPort(SerialPort serialPort, byte[] order);

    /**
     * 关闭串口
     *
     * @param serialport
     *            待关闭的串口对象
     */
    public  void closePort(SerialPort serialPort);
}
