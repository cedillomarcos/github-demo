package reid;

import java.util.Random;
import java.io.*;

interface SharedConstants {
	int NO = 0;
	int YES = 1;
	int MAYBE = 2;
	int LATER = 3;
	int SOON = 4;
	int NEVER = 5;
}

class Question implements SharedConstants {
	Random rand = new Random();

	int ask() {
		int prob = (int)(100 * rand.nextDouble());
		if (prob < 30)
			return NO;
		else if (prob < 60)
			return YES;
		else if (prob < 75)
			return LATER;
		else if (prob < 98)
			return SOON;
		else
			return NEVER;
	}
}


class AskMe implements SharedConstants {
	static void answer(int result) {
		switch(result){
			case NO:
				System.out.println("NO");
				break;
			case YES:
				System.out.println("YES");
				break;
			case MAYBE:
				System.out.println("MAYBE");
				break;
			case LATER:
				System.out.println("LATER");
				break;
			case SOON:
				System.out.println("SOON");
				break;
			case NEVER:
				System.out.println("NEVER");
				break;
		}
	}
}
/*
interface Callback {
	void callback(int param);
}

class Client implements Callback {
	public void callback(int param) {
		System.out.println("callback: " + param);
	}

	void nonIfaceMeth() {
		System.out.println("nonIfaceMeth");
	}
}

class AnotherClient implements Callback {
	public void callback(int param) {
		System.out.println("Another version of callback");
		System.out.println("param squared is " + (param*param));
	}
}
*/

class NewThread implements Runnable {
	String name;
	Thread t;

	NewThread(String threadname) {
		name = threadname;
		t = new Thread(this, name);
		System.out.println("New Thread: " + t);
		t.start();
	}

	public void run() {
		try {
			for (int i=5; i>0; i--) {
				System.out.println(name + ": " + i);
				Thread.sleep(1000);
			}
		}catch(InterruptedException e) {
			System.out.println(name + "Interrupted");
		}
		System.out.println(name + " exiting.");
	}
}

class Clicker implements Runnable {
	Thread t;
	int click = 0;
	private volatile boolean running = true;

	public Clicker(int p){
		t = new Thread(this);
		t.setPriority(p);
	}

	public void run() {
		while(running) {
			click++;
		}
	}

	public void start() {
		t.start();
	}

	public void stop() {
		running = false;
	}
}

public class test {
	public static void main(String[] args) throws IOException{
		//Callback cb = new Client();
		//cb.callback(11);
		/*
		Callback cb = new Client();
		AnotherClient ob = new AnotherClient();

		cb.callback(11);

		cb = ob;
		cb.callback(11);*/
		/*
		Question q = new Question();
		AskMe.answer(q.ask());
		AskMe.answer(q.ask());
		AskMe.answer(q.ask());
		AskMe.answer(q.ask());*/
		/*
		new NewThread("One");
		new NewThread("Two");
		new NewThread("Three");

		try {
			Thread.sleep(10000);
		}catch(InterruptedException e) {
			System.out.println("Main thread Interrupted");
		}
		System.out.println("Main thread exiting.");*/

		/*
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		Clicker hi = new Clicker(Thread.NORM_PRIORITY + 2);
		Clicker lo = new Clicker(Thread.NORM_PRIORITY - 2);

		lo.start();
		hi.start();

		try {
			Thread.sleep(10000);
		}catch(InterruptedException e) {
			System.out.println("Main thread Interrupted.");
		}

		lo.stop();
		hi.stop();

		try {
			lo.t.join();
			hi.t.join();
		}catch(InterruptedException e) {
			System.out.println("InterruptedException caught.");
		}

		System.out.println("Low-priority thread: " + lo.click);
		System.out.println("High-priority thread: " + hi.click);*/

		/*
		char c;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter characters, 'q' to quit.");

		//read characters
		do {
			c = (char)br.read();
			System.out.println(c);
		}while(c != 'q');*/

		/*
		String str;

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Enter lines of text.");
		System.out.println("Enter 'stop' to quit.");		

		do {
			str = br.readLine();
			System.out.println(str);
		}while(!str.equals("stop"));*/

		/*
		int b;

		b = 'A';
		System.out.write(b);
		System.out.write('\n');*/

		/*
		PrintWriter pw = new PrintWriter(System.out, true);
		pw.println("This is a string.");
		int i = 7;
		double d = 4.5e-7;

		pw.println(i);
		pw.println(d);*/

		int i;
		FileInputStream fin;
		FileOutputStream fout;

		try {
			//open input file
			try {
				fin = new FileInputStream(args[0]);
			}catch(FileNotFoundException e) {
				System.out.println("Input File Not Found");
				return;
			}

			//open output file
			try {
				fout = new FileOutputStream(args[1]);
			}catch(FileNotFoundException e) {
				System.out.println("Error Opening Output File");
				return;
			}
			
		}catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("Usage: CopyFile From To");
			return;
		}

		try {
			do {
				i = fin.read();
				if(i != -1)
					fout.write(i);
				}while(i != -1);
		}catch(IOException e) {
			System.out.println("File Error");
		}

		fin.close();
		fout.close();
	}
}