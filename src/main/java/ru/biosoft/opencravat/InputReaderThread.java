package ru.biosoft.opencravat;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;


public class InputReaderThread {

	private static final Logger log = Logger.getLogger(InputReaderThread.class.getName() );
	
	public static void read(BufferedReader reader, Consumer<String> consumer)
	{
		new Thread(new Runnable() {
			@Override
			public void run() {
				String line;
				try {
					while((line = reader.readLine()) != null)
					{
						consumer.accept(line);
					}
				} catch (IOException e) {
					log.log(Level.SEVERE, "", e);
				}
			}
		}).start();
	}
	
}
