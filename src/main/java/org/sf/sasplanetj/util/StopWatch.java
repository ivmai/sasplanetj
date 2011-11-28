package org.sf.sasplanetj.util;

public final class StopWatch {

	private static final boolean useNanoTime;

	private long startValue;

	private boolean isValid; // start() has been called

	static {
		boolean hasNanoTime = false;
		// Check whether JRE exports nanoTime()
		try {
			System.nanoTime();
			hasNanoTime = true;
		} catch (NoSuchMethodError e) {
			System.out
					.println("No System.nanoTime available, using currentTimeMillis...");
		}
		useNanoTime = hasNanoTime;
	}

	public void start() {
		startValue = useNanoTime ? System.nanoTime() : System
				.currentTimeMillis();
		isValid = true;
	}

	public int currentMillis() {
		return isValid ? (int) (useNanoTime ? (System.nanoTime() - startValue)
				/ (1000L * 1000L) : System.currentTimeMillis() - startValue)
				: -1;
	}
}