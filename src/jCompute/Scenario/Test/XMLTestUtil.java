package jCompute.Scenario.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.configuration.XMLConfiguration;

import jCompute.Scenario.ConfigurationInterpreter;

public class XMLTestUtil
{
	public static void main(String[] args)
	{
		System.out.println("XML TESTER");
		// File file = new File("scenarios/SAPP/batch/BatchRun-ViewRange.xml");
		File file = new File("scenarios/HenonMap/default.scenario");

		XMLConfiguration scenario = new XMLConfiguration();

		scenario.setSchemaValidation(true);
		ConfigurationInterpreter sc = new ConfigurationInterpreter();
		// LotkaVolterraScenario lv = new LotkaVolterraScenario();

		BufferedReader bufferedReader = null;
		try
		{
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO_8859_1"));

		}
		catch(FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		StringBuilder editor = new StringBuilder();
		String line = "";
		try
		{
			while(line != null)
			{
				line = bufferedReader.readLine();
				editor.append(line);

				// scenarioEditor.insert(sCurrentLine,
				// scenarioEditor.getLineCount());
				// System.out.println(sCurrentLine);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		System.out.println(editor.toString());

		sc.loadConfig(editor.toString());

		// sc.getScenarioXMLText();
		sc.dumpXML();

		System.exit(0);
	}

}
