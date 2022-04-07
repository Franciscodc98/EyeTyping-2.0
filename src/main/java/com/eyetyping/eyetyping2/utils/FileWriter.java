package com.eyetyping.eyetyping2.utils;

import lombok.Data;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FileWriter implements Closeable {

	private PrintWriter pw;
	private File file;

	public FileWriter(String fileName) throws FileNotFoundException {
		file = new File(fileName);
		this.pw = new PrintWriter(new FileOutputStream(file, true));
	}

	public void writePhrase(String phrase){
		pw.println(phrase);
		pw.flush();
	}

	public void writePhrases(List<String> phrases){
		for (String phrase : phrases)
			pw.println(phrase);
		pw.flush();
	}

	public void writeDataFromListToCsv(List<String> data){
		pw.println(String.join(",", data));
		pw.flush();
	}

	public boolean isFileEmpty(){
		return file.length() == 0;
	}


	@Override
	public void close(){
		pw.close();
	}
}