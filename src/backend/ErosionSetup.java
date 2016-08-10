/*
 * SpringMapEdit -- A 3D map editor for the Spring engine
 *
 * Copyright (C) 2008-2009  Heiko Schmitt <heikos23@web.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
/**
 * ErosionSetup.java 
 * Created on 27.02.2009
 * by Heiko Schmitt
 */
package backend;

import java.io.File;
import java.io.IOException;

import backend.tdf.TDFDocument;
import backend.tdf.TDFKeyValue;
import backend.tdf.TDFSection;

/**
 * @author Heiko Schmitt
 *
 */
public class ErosionSetup
{
	public boolean useAlternativeWetMethod;
	
	public int wetIterations;
	public float wetDropletHeight;
	public float wetEvaporateAmount;
	
	public int wet2Iterations;
	public float wet2BreakHeight;
	
	public int dryIterations;
	public float dryBreakHeight;

	public static final String erosionScriptPath = "erosionscripts/";
	
	private enum ESSections
	{
		EROSIONSETUP,
		WET,
		WET2,
		DRY,
	}
	private enum ESKeys
	{
		useAlternativeWetMethod,
		Iterations,
		DropletHeight,
		EvaporateAmount,
		BreakHeight
	}
	
	/**
	 * 
	 */
	public ErosionSetup()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * Load setup from given tdfFile
	 * @param tdfFile
	 */
	public ErosionSetup(File tdfFile) throws IOException
	{
		try
		{
			TDFDocument doc = new TDFDocument(tdfFile);
			TDFSection main = doc.getSection(ESSections.EROSIONSETUP.name());
			if (main == null) throw new IOException("No \"" + ESSections.EROSIONSETUP.name() + "\" section found in " + tdfFile.getName());
			
			//Extract useAlternativeWetMethod
			TDFKeyValue kv = main.getKeyValue(ESKeys.useAlternativeWetMethod.name());
			if (kv == null) throw new IOException("No \"" + ESKeys.useAlternativeWetMethod.name() + "\" key found in " + tdfFile.getName());
			useAlternativeWetMethod = Boolean.parseBoolean(kv.getValue());
			
			//WET
			TDFSection wet = main.getSection(ESSections.WET.name());
			if (wet == null) throw new IOException("No \"" + ESSections.WET.name() + "\" section found in " + tdfFile.getName());
			
			kv = wet.getKeyValue(ESKeys.Iterations.name());
			if (kv == null) throw new IOException("No \"" + ESKeys.Iterations.name() + "\" key (wet) found in " + tdfFile.getName());
			wetIterations = Integer.parseInt(kv.getValue());
			
			kv = wet.getKeyValue(ESKeys.DropletHeight.name());
			if (kv == null) throw new IOException("No \"" + ESKeys.DropletHeight.name() + "\" key (wet) found in " + tdfFile.getName());
			wetDropletHeight = Float.parseFloat(kv.getValue());
			
			kv = wet.getKeyValue(ESKeys.EvaporateAmount.name());
			if (kv == null) throw new IOException("No \"" + ESKeys.EvaporateAmount.name() + "\" key (wet) found in " + tdfFile.getName());
			wetEvaporateAmount = Float.parseFloat(kv.getValue());
			
			//WET2
			TDFSection wet2 = main.getSection(ESSections.WET2.name());
			if (wet2 == null) throw new IOException("No \"" + ESSections.WET2.name() + "\" section found in " + tdfFile.getName());
			
			kv = wet2.getKeyValue(ESKeys.Iterations.name());
			if (kv == null) throw new IOException("No \"" + ESKeys.Iterations.name() + "\" key (wet2) found in " + tdfFile.getName());
			wet2Iterations = Integer.parseInt(kv.getValue());
			
			kv = wet2.getKeyValue(ESKeys.BreakHeight.name());
			if (kv == null) throw new IOException("No \"" + ESKeys.BreakHeight.name() + "\" key (wet2) found in " + tdfFile.getName());
			wet2BreakHeight = Float.parseFloat(kv.getValue());
			
			//DRY
			TDFSection dry = main.getSection(ESSections.DRY.name());
			if (dry == null) throw new IOException("No \"" + ESSections.WET2.name() + "\" section found in " + tdfFile.getName());
			
			kv = dry.getKeyValue(ESKeys.Iterations.name());
			if (kv == null) throw new IOException("No \"" + ESKeys.Iterations.name() + "\" key (wet2) found in " + tdfFile.getName());
			dryIterations = Integer.parseInt(kv.getValue());
			
			kv = dry.getKeyValue(ESKeys.BreakHeight.name());
			if (kv == null) throw new IOException("No \"" + ESKeys.BreakHeight.name() + "\" key (wet2) found in " + tdfFile.getName());
			dryBreakHeight = Float.parseFloat(kv.getValue());
		}
		catch (NumberFormatException nfe)
		{
			throw new IOException(nfe);
		}
	}
	
	public void saveToFile(File tdfFile)
	{
		//Delete old file
		if (tdfFile.exists()) tdfFile.delete();
		
		TDFDocument doc = new TDFDocument();
		TDFSection main = new TDFSection(ESSections.EROSIONSETUP.name());
		doc.addSection(null, main);
		
		TDFKeyValue kv = new TDFKeyValue(ESKeys.useAlternativeWetMethod.name(), Boolean.toString(useAlternativeWetMethod));
		main.addKeyValue(kv);
		
		//WET
		TDFSection wet = new TDFSection(ESSections.WET.name());
		doc.addSection(main, wet);
		
		kv = new TDFKeyValue(ESKeys.Iterations.name(), Integer.toString(wetIterations));
		wet.addKeyValue(kv);
		
		kv = new TDFKeyValue(ESKeys.DropletHeight.name(), Float.toString(wetDropletHeight));
		wet.addKeyValue(kv);
		
		kv = new TDFKeyValue(ESKeys.EvaporateAmount.name(), Float.toString(wetEvaporateAmount));
		wet.addKeyValue(kv);
		
		//WET2
		TDFSection wet2 = new TDFSection(ESSections.WET2.name());
		doc.addSection(main, wet2);
		
		kv = new TDFKeyValue(ESKeys.Iterations.name(), Integer.toString(wet2Iterations));
		wet2.addKeyValue(kv);
		
		kv = new TDFKeyValue(ESKeys.BreakHeight.name(), Float.toString(wet2BreakHeight));
		wet2.addKeyValue(kv);
		
		//DRY
		TDFSection dry = new TDFSection(ESSections.DRY.name());
		doc.addSection(main, dry);
		
		kv = new TDFKeyValue(ESKeys.Iterations.name(), Integer.toString(dryIterations));
		dry.addKeyValue(kv);
		
		kv = new TDFKeyValue(ESKeys.BreakHeight.name(), Float.toString(dryBreakHeight));
		dry.addKeyValue(kv);
		
		//Save...
		doc.save(tdfFile);
	}
}
