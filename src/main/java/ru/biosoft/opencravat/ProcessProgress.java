package ru.biosoft.opencravat;

public interface ProcessProgress {
	void stdout(String msg);
	void stderr(String msg);
	void finished(int exitCode);
}
