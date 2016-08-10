/**
 * SM3Layer.java 
 * Created on 14.12.2009
 * by Heiko Schmitt
 */
package backend.sm3;

/**
 * @author Heiko Schmitt
 *
 */
public class SM3Layer
{
	public enum Operation
	{
		ADD,
		MUL,
		BLEND
	}
	
	//Which stage is this layer?
	public int texStage;
	
	//Operation
	public Operation operation;
	
	//Source
	public String texturemapFile;
	public String normalmapFile;
	public int tileSize;
	
	//Blender
	public boolean hasBlendmap;
	public String blendmapFile;
	
	//Editor Data
	public float[][] blendmap;
	
	/**
	 * 
	 */
	public SM3Layer()
	{
	}

	/**
	 * Returns a part of FragmentShader which
	 * @return
	 */
	public String getFragmentShaderPart()
	{
		/*
		 * Shader will look something like:
		 * Note this should/will be generated externally..., so:
		 * FIXME move outside this class
		  
		 uniform sampler2D tex0;
		 uniform sampler2D bumpTex0;
		 
		 uniform sampler2D tex1;
		 uniform sampler2D blendTex1;
		 uniform sampler2D bumpTex1;
		 
		 uniform sampler2D tex2;
		 uniform sampler2D blendTex2;
		 uniform sampler2D bumpTex2;
		 
		 uniform sampler2D tex3;
		 uniform sampler2D blendTex3;
		 uniform sampler2D bumpTex3;
		 
		 void main()
		 {
		 	blendAmount1 = texture2D(blendTex1, texCoords[1].st);
		 	col0 = (1 - blendAmount1) * texture2D(tex0, texCoords[0].st);
		 	
		 	blendAmount2 = texture2D(blendTex2, texCoords[2].st);
		 	col1 = (1 - blendAmount2) * (blendAmount1 * texture2D(tex1, texCoords[1].st));
		 	
		 	blendAmount3 = texture2D(blendTex3, texCoords[3].st);
		 	col2 = (1 - blendAmount3) * (blendAmount2 * texture2D(tex2, texCoords[2].st));
		 	
		 	col3 = (blendAmount3 * texture2D(tex3, texCoords[3].st));
		 	
			gl_FragColor = col0 + col1 + col2 + col3;
			
			/*
			 * This approach requires two temp-var per layer, but it allows for parallel calculation, as each
			 * layer is independent from other layers 
			 *
		 }
		 
		 */
		return null;
	}
}
