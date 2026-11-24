package ru.biosoft.opencravat;

import java.util.ArrayList;
import java.util.List;

public class ProcessResult implements ProcessProgress {
	public List<String> stdoutLines = new ArrayList<>();
	public List<String> stderrLines = new ArrayList<>();
	public int exitCode;
	@Override
	public void stdout(String msg) {
		stdoutLines.add(msg);
	}
	@Override
	public void stderr(String msg) {
		stderrLines.add(msg);
	}
	@Override
	public void finished(int exitCode) {
		this.exitCode = exitCode;
	}
}
