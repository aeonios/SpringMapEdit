SpringMapEdit: A standalone map editor for the Spring RTS engine.

--------(C)2008-2009 --------
https://springrts.com/phpbb/viewtopic.php?f=13&p=575916#p575916

For licensing stuff see: LICENSE.TXT

Table of Contents

1. About
2. Requirements
3. Installation / Startup
4. Controls & Settings
5. FAQ
6. HowTo
7. Troubleshooting
8. Contact

------------------------------------------
1. About
------------------------------------------

SpringMapEdit is an application for editing the various parts of which a Spring map (http://springrts.com/) consists within a full 3D environment.
It was written by Heiko Schmitt in Java and uses SWT and JOGL libraries.

Currently supported Features:

-General map editing
	-Flipping/Mirroring/Shifting of all or portions of the maps with customizable settings

-Heightmap editing
	-Raise/Lower/Set/Smooth/Randomize all with customizable brushes
	-Hydraulic/Thermic Erosion of whole map, or parts

-Texture editing
	-Paint with loaded texture (Stamp (respects texture alpha), Set, Blend (respects brush pattern))
	-Autogenerate based on height/steepness and given textures. Setup via textfiles.

-Feature placement
	-add/remove/rotate Features

-Slopemap/Typemap/Metalmap/Vegetationmap viewing and editing and toggleing overlay texturemap.

-Export directly to SM2 map format (.smt + .smf)

-Import directly from SM2 map format (.smt + .smf)

-Import and Export any map from various formats
	BMP 8/24/32 bit
	RAW 16 bit


------------------------------------------
2. Requirements
------------------------------------------

Supported Platforms:
all which support Java, SWT, and JOGL, this includes, but is not limited to:
-Windows (windows 10 might need tweaks in the startup script)
-Linux
-OSX     (borked)

Hardware:
-OpenGL 2.0 capable Graphics Card (Geforce5 and up)
-Plenty of RAM (depending on mapsize)

Software
-Java (Openjdk 1.7 recommended - http://www.java.com)
-SWT 4.5 library (included in this package - http://www.eclipse.org/swt/ )
-JOGL 1.1.1 library (included in this package - https://jogl.dev.java.net/ )


If it does not startup properly, you may need to get the correct libraries for your platform,
and check some dependencies SWT and JOGL have. (Especially on OSX (GTK?))

------------------------------------------
3. Installation / Startup
------------------------------------------

Just extract contents of this archive into any directory.
Then start the appropriate script.

Windows: double click "start_win64.bat".
Linux:   cd to the SME directory and type "chmod +x start_linux_x64.sh" to make the script executable, then type "./start_linux_x64.sh" to run it.
OSX:     does not work, library issues. :(

If you get crashes when editing large maps, or it does not start at all,
try editing the startup file.
There is a parameter which reads like:
-Xmx1024m
The 1024 is the maximum amount of RAM which Java is allowed to use.
Set it to your needs, but make it a multiple of 128.
If Java can not find a batch of RAM as large as given number, it won't start, so just set it a little lower than your real RAM.

------------------------------------------
4. Controls & Settings
------------------------------------------

Movement:

ARROWS					Move around
Scrollwheel/Pageup/Pagedown	Zoom in/out
Middle Mouse Button			Mouselook 	
L							Toggle Mouselook
MOUSE						Look around / move brush / actions
SHIFT						Hold to move around faster
CTRL						Hold to move slower
B							Toggle Invert Mouse Y


Editing:

E		Hydraulic Erode Heightmap
T		Thermal Erode Heightmap
P		Convert heightmap into 15 discrete heightlevels (terracing)
S		Smooth complete Heightmap
F8		Quicksave into RAM
F12		Quickload from RAM

G		Autogenerate Texture (Currently NOT customizable... needs a _good_ GUI...)

F1		View Slopemap
F2		View Typemap
F3		View Vegetationmap
F4		View Metalmap
F5		View Texturemap
F6		Blend Texturemap into other Views

System:

Q		Toggle "nice Shader Water"
W		Wireframe mode
O		Toggle Terrain LOD
F		Toggle Texture filtering
H		Toggle Lighting
N		Toggle "Smooth Normal Calculation"
M		Toggle "faster smooth Normal Calculation"
V		Toggle "Feature lighting"

Settings are stored within
config/keys.cfg and
config/settings.cfg

Some of those settings are not customizable within the GUI,
so you may wish to alter these files directly.

settings.cfg:

displayWidth	1024			initial window width
displayHeight	768				initial window height
featureTexSize	128				size of feature selektor buttons
smoothNormals	true			Key: M
fastNormals	false				Key: N
fancyWater	false				Key: Q
useLighting	true				Key: H
waterMapExtend	200				How far the water will extend beyond terrain
moveSun	false
drawSun	false
onlyOutlineBrush	true		If Brush will be solid, or outlined. (If set to false could impact performance)
filterTextures	true
compressTextures	true		If textures will be compressed (affects only Texturemap&Features)
maxFeaturesToDisplay	10240	Maximum number of features that will be rendered
vsync	false
featureLighting	false			If Features recieve lighting, or not
renderFeatureLOD	3			Last Terrain-LOD which will show features
useLOD	false					Enable/Disable LOD
outputPerfDebug	false			Output performance debugging info
mouseLook	false				Key: SPACE
invertY	true					If Y-Axis is inverted while Mouselook is active
sensitivity	5.0					Mouse sensitivity while looking around
slowSpeed	1.0					Slow movespeed (CTRL)
normalSpeed	8.0					Normal movespeed
fastSpeed	16.0				Fast movespeed (SHIFT)
compressQuicksave	false		if quicksaves should be compressed into RAM. Should only be used if RAM is low.
dialogAlwaysOnTop	true		if dialog is always on top
quitWithoutAsking	false		if a confirmation dialog should be displayed on exit
lodDist	800						the distance away from the camera up to which maximum detail is used.
Up to the distance of the first value, LODLevel 0 will be used,
up to the distance of the second value, LODLevel 1 will be uses and so on...

If you mess up, just delete the config.
A new one will be created with proper defaults.


keys.cfg:

Basically it is just:
<keyCode>	CommandIdentifier

keyCode is a mixture of modifiers and an actual keycode.
Since the codes are from SWT, they do not completely correspond to ASCII.
Normal letters do match, but special keys do not (F1, F2, Arrows...)

Modifiers are "SHIFT", "CTRL", "ALT".
If used, they MUST be used in above order.

SHIFTCTRLALT114	RENDER_ALL
would be SHIFT+CTRL+ALT+R to execute RENDER_ALL

If something is bound to "R" and you press SHIFT+R with nothing bound to
SHIFT+R, the command for R will be executed.
This fallback is necessary for shift+arrow / ctrl+arrow, to work like expected.

"unbindall" is a special command which removes all previous bindings.
This is useful to get rid of the default bindings, which are always loaded before loading keys.cfg itself.


Hmm, while writing this stuff, I could have been implementing a GUI for keybindings... ;)


------------------------------------------
5. FAQ
------------------------------------------


5.1 Why do the Sliders jump around when selecting a different Brush-Mode?

Each Brush-Mode (each SubMode too) has its own settings,
which are reflected by the Strength and Size Sliders.


5.2. When importing SM2 it complains about missing features?

If you import a map, you have to make sure all required files are available at program startup.
Feature definitions (.tdf) inside "features"
Models (.s30) inside "objects3d"
Textures (.tga/.dds) inside "unittextures"
This is because existing Features are only scanned once at startup.


------------------------------------------
6. HowTo
------------------------------------------

6.1 How to disable water?
Just set the Waterheight in the edit->map properties dialog to -1.

------------------------------------------
7. Troubleshooting
------------------------------------------

7.1 Slow performance on Vista. I am using Aero (3D Desktop)

Maybe a driver issue, maybe something else.
Turn Aero off, then it should work normally.


7.2 It crashes on startup, mentioning an external nvogl.dll.

Somehow it crashes with Nvidia 177.92 Beta drivers on my 8800GT, 
if "Threaded optimizations" are set to "off". 
(Which you may have done in respect to Spring)

Solution: Open your Nvidia Control Panel, and set "Threaded optimizations" back to "Auto".


7.3 It won't start up. Out of Memory problems.

See section 3.


7.4 File dialogs do not seem to appear.

There is a bug in java 6 update 26, which prevented the file dialogs to show on Win7.
If you encounter the problem, just update your java installation.
I tested it again with java 6 update 31, and the dialogs appeared again.


7.5 Java cannot be found on windows.

Find the location of where java is installed and set the %JAVA_HOME% variable in your terminal to that path.


------------------------------------------
8. Contact
------------------------------------------

Post in the offcial thread (https://springrts.com/phpbb/viewtopic.php?f=13&p=575916#p575916) or send an email to code_man@cybnet.ch
