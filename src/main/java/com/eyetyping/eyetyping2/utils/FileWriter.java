package com.eyetyping.eyetyping2.utils;

import lombok.Data;
import java.io.*;

@Data
public class FileWriter implements Closeable {

	private PrintWriter pw;
	
	public FileWriter(File file) throws FileNotFoundException {
		this.pw = new PrintWriter(file);
	}

	@Override
	public void close(){
		pw.close();
	}
}