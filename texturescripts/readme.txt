Short explanation of TexGen Scripts
-----------------------------------

File Syntax is normal .tdf which you may be used to.
Here is just a complete script (identical to supplied default.tdf) as an example,
with comments for explanation.

[TEXTUREGENERATORSETUP]
{
	Levels=4;					//Number of Texture-height-levels used
	[FLAT]						//Textures used in FLAT areas, for different heightlevels
	{
		Texture1=beach2.png;	//flat heightlevel 1 texture
		Texture2=grass3.png;	//flat heightlevel 2 texture
		Texture3=stone.png;		//...
		Texture4=ice.png;
	}
	[STEEP]						//Textures used in STEEP areas, for different heightlevels
	{
		Texture1=sandstone.png;	//steep heightlevel 1 texture
		Texture2=stone.png;		//...
		Texture3=stone.png;
		Texture4=stone.png;
	}
	[HEIGHTTRANSITIONS]			//These values define the different heightlevels
	{							//Height ranges from 0 to 1. With 1 beeing maxheight
		End1=0.3;				//End of level 1. Solid Texture1 is used from 0 up to "End1" value

		Start2=0.4;				//Start of level 2. Blending between Tex1 and Tex2 occurs between "End1" and "Start2"
		End2=0.5;				//End of level 2. Solid Tex2 is used from "Start2" to "End2"

		Start3=0.6;				//Start of level 3. Blending between Tex2 and Tex3 occurs between "End2" and "Start3"
		End3=0.7;				//End of level 3. Solid Tex3 is used from "Start3" to "End3"

		Start4=0.8;				//Start of level 4. Blending between Tex3 and Tex4 occurs between "End3" and "Start4"
								//Solid Tex4 is used from "Start4" to 1

								//There are always ((levels - 1) * 2) entries here.
								//Only exception is levels==1, then you may omit [HEIGHTTRANSITIONS] Section completely
								//First entry is always "End1", and last entry is always "StartX"
	}
	[STEEPTRANSITIONS]			//These values define flat and steep
	{
		Flat1=0.005;			//Heightlevel 1: up to Flat1 steepness use solid flat Tex1
		Steep1=0.03;			//Heightlevel 1: Blending between flat Tex1 and steep Tex1 occurs between "Flat1" and "Steep1"
								//Heightlevel 1: more steepness than Steep1 and solid steep Tex1 is used

		Flat2=0.005;			//...
		Steep2=0.03;

		Flat3=0.005;
		Steep3=0.03;

		Flat4=0.005;
		Steep4=0.03;
								//There are always (levels * 2) entries here. 2 per Heightlevel.
	}
}

Actually there is a 4 way blending system in place:

FLAT level 1 <-> STEEP level 1
   <->               <->
FLAT level 2 <-> STEEP level 2



Things to come (maybe...):

-multiple setups per map. areas defined by something similar to typemap.
	(area1 uses this setup, area2 this, and area3 another)
-baked in (multiple) detail bumpmaps together with lighting
-baked in terrain shadows

