varying vec4 color;
uniform sampler2D reflectionTex;

void main(void)
{
	vec4 texel;
	texel = texture2D(reflectionTex, gl_TexCoord[0].st);

	gl_FragColor = vec4(color * texel);
	
/* blabla */
}
