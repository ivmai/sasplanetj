package sasplanetj.gps;

public class SerialReaderTest extends Thread {

	public static void main(String[] args) throws Exception {
		System.out.println("Testing SerialReader");

		SerialReader serialReader;
		if (args.length > 0) {
			String port = args[0];
			int baudRate = args.length > 1 ? new Integer(args[1]).intValue()
					: 9600;
			serialReader = new SerialReader(port, baudRate);
		} else {
			serialReader = new SerialReader();
		}
		serialReader.start();
	}

}
