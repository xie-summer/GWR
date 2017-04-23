package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * <title>"JConsole"-Java模拟控制台</title>
 * <p>
 * 这个类是用Java实现Windows CMD控制台。通过获得当前运行进程，
 * 执行相关命令信息。实现交互式的CMD控制台。<br>
 * <br>
 * 最近有个想法,用Java实现像Windows 下CMD控制台一样,可以进行交互式地运行命令进行操作,
 * 今天终于写了个简单的例子。
 *
 * </p>
 * @author 魏仁言
 * @version 0.9
 */
public class JConsole {
	/**
	 * 获取执行当前命令的进程
	 *
	 * @param strCommand
	 *            运行命令
	 * @return pro Precess
	 */
	private Process getProcess(String strCommand) {
		Process pro = null;
		try {
			// 运行当前命令并获得此运行进程
			pro = Runtime.getRuntime().exec(strCommand);
		} catch (IOException e) {
			// when cause IOException ,Print the Exception message;
			System.out.println("Run this command:" + strCommand + "cause Exception ,and the message is " + e.getMessage());
		}
		// 返回运行命令的进程
		return pro;
	}

	/**
	 * 获取当前进程标准输入流
	 *
	 * @param pro
	 *            当前进程
	 * @return in InputStream
	 */
	public InputStream getInputSreamReader(Process pro) {
		InputStream in = null;

		// 获得当前进程的输入流
		in = pro.getInputStream();

		// 返回输入流
		return in;
	}

	/**
	 * 获取当前进程的错误输入流
	 *
	 * @param pro
	 *            当前进程
	 * @return error InputStream
	 */
	public InputStream getErrorSreamReader(Process pro) {
		InputStream error = null;

		// 获得当前进程的错误流
		error = pro.getErrorStream();

		// 返回错误流
		return error;
	}

	/**
	 * 打印输入流中的内容到标准输出
	 *
	 * @param in
	 *            InputStream 输入流
	 */
	public void printMessage(final InputStream in) {
		Runnable runnable = new Runnable() {

			/*
			 * (non-Javadoc)
			 *
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				try {
					/*
					 * int ch; do { ch = in.read();
					 *
					 * if (ch < 0) return; System.out.print((char) ch);
					 * System.out.flush(); } while (true);
					 */
					BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
					String lines;
					while (true) {
						lines = buffer.readLine();
						if (lines == null)
							return;
						System.out.println("" + lines);
						System.out.flush();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		};
		Thread thread;
		thread = new Thread(runnable);
		thread.start();
	}

	/**
	 * 初始化CMD控制台。
	 */
	public void run() {
		Process pro;

		InputStream input;
		pro = getProcess("cmd");

		input = getInputSreamReader(pro);

		printMessage(input);
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// 运行子进程
		Process pro = null;

		Scanner in = new Scanner(System.in);

		// 输入流
		InputStream input;

		// 错误流
		InputStream err;

		JConsole jconsole = new JConsole();

		// 初始化CMD控制台
		jconsole.run();

		// 从输入中取得要执行的命令，并打印反馈结果信息
		while (in.hasNextLine()) {

			String strCMD = in.nextLine();

			if (strCMD.equals("exit")) {
				System.exit(0);
			}

			// 执行输入的命令
			pro = jconsole.getProcess("cmd /E:ON /c " + strCMD);

			input = jconsole.getInputSreamReader(pro);

			err = jconsole.getErrorSreamReader(pro);

			// 打印命令运行结果信息
			int strTmp = input.read();
			if (strTmp == -1) {
				jconsole.printMessage(err);
			} else {
				jconsole.printMessage(input);
			}
		}
	}

}