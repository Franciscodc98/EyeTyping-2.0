package com.eyetyping.eyetyping2.utils;

import lombok.Data;
import java.io.*;
import java.util.List;

@Data
public class FileWriter implements Closeable {

	private PrintWriter pw;

	public FileWriter(String fileName) throws FileNotFoundException {
		this.pw = new PrintWriter(fileName);
	}

	public void WritePhrase(String phrase){
		pw.println(phrase);
	}

	public void WritePhrases(List<String> phrases){
		for (String phrase : phrases)
			pw.println(phrase);
	}

	@Override
	public void close(){
		pw.close();
	}
}