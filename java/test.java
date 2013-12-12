package reid;

import java.util.Random;

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

public class test {
	public static void main(String[] args) {
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

		new NewThread("One");
		new NewThread("Two");
		new NewThread("Three");

		try {
			Thread.sleep(10000);
		}catch(InterruptedException e) {
			System.out.println("Main thread Interrupted");
		}
		System.out.println("Main thread exiting.");
	}
}