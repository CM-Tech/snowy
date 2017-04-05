package minithree

package object THREE {
  var REVISION: String                               = THREE.REVISION
  var CullFaceNone: CullFace                         = THREE.CullFaceNone
  var CullFaceBack: CullFace                         = THREE.CullFaceBack
  var CullFaceFront: CullFace                        = THREE.CullFaceFront
  var CullFaceFrontBack: CullFace                    = THREE.CullFaceFrontBack
  var FrontFaceDirectionCW: FrontFaceDirection       = THREE.FrontFaceDirectionCW
  var FrontFaceDirectionCCW: FrontFaceDirection      = THREE.FrontFaceDirectionCCW
  var BasicShadowMap: ShadowMapType                  = THREE.BasicShadowMap
  var PCFShadowMap: ShadowMapType                    = THREE.PCFShadowMap
  var PCFSoftShadowMap: ShadowMapType                = THREE.PCFSoftShadowMap
  var FrontSide: Side                                = THREE.FrontSide
  var BackSide: Side                                 = THREE.BackSide
  var DoubleSide: Side                               = THREE.DoubleSide
  var NoShading: Shading                             = THREE.NoShading
  var FlatShading: Shading                           = THREE.FlatShading
  var SmoothShading: Shading                         = THREE.SmoothShading
  var NoColors: Colors                               = THREE.NoColors
  var FaceColors: Colors                             = THREE.FaceColors
  var VertexColors: Colors                           = THREE.VertexColors
  var NoBlending: Blending                           = THREE.NoBlending
  var NormalBlending: Blending                       = THREE.NormalBlending
  var AdditiveBlending: Blending                     = THREE.AdditiveBlending
  var SubtractiveBlending: Blending                  = THREE.SubtractiveBlending
  var MultiplyBlending: Blending                     = THREE.MultiplyBlending
  var CustomBlending: Blending                       = THREE.CustomBlending
  var AddEquation: BlendingEquation                  = THREE.AddEquation
  var SubtractEquation: BlendingEquation             = THREE.SubtractEquation
  var ReverseSubtractEquation: BlendingEquation      = THREE.ReverseSubtractEquation
  var ZeroFactor: BlendingDstFactor                  = THREE.ZeroFactor
  var OneFactor: BlendingDstFactor                   = THREE.OneFactor
  var SrcColorFactor: BlendingDstFactor              = THREE.SrcColorFactor
  var OneMinusSrcColorFactor: BlendingDstFactor      = THREE.OneMinusSrcColorFactor
  var SrcAlphaFactor: BlendingDstFactor              = THREE.SrcAlphaFactor
  var OneMinusSrcAlphaFactor: BlendingDstFactor      = THREE.OneMinusSrcAlphaFactor
  var DstAlphaFactor: BlendingDstFactor              = THREE.DstAlphaFactor
  var OneMinusDstAlphaFactor: BlendingDstFactor      = THREE.OneMinusDstAlphaFactor
  var DstColorFactor: BlendingSrcFactor              = THREE.DstColorFactor
  var OneMinusDstColorFactor: BlendingSrcFactor      = THREE.OneMinusDstColorFactor
  var SrcAlphaSaturateFactor: BlendingSrcFactor      = THREE.SrcAlphaSaturateFactor
  var MultiplyOperation: Combine                     = THREE.MultiplyOperation
  var MixOperation: Combine                          = THREE.MixOperation
  var AddOperation: Combine                          = THREE.AddOperation
  var UVMapping: MappingConstructor                  = THREE.UVMapping
  var CubeReflectionMapping: MappingConstructor      = THREE.CubeReflectionMapping
  var CubeRefractionMapping: MappingConstructor      = THREE.CubeRefractionMapping
  var SphericalReflectionMapping: MappingConstructor = THREE.SphericalReflectionMapping
  var SphericalRefractionMapping: MappingConstructor = THREE.SphericalRefractionMapping
  var RepeatWrapping: Wrapping                       = THREE.RepeatWrapping
  var ClampToEdgeWrapping: Wrapping                  = THREE.ClampToEdgeWrapping
  var MirroredRepeatWrapping: Wrapping               = THREE.MirroredRepeatWrapping
  var NearestFilter: TextureFilter                   = THREE.NearestFilter
  var NearestMipMapNearestFilter: TextureFilter      = THREE.NearestMipMapNearestFilter
  var NearestMipMapLinearFilter: TextureFilter       = THREE.NearestMipMapLinearFilter
  var LinearFilter: TextureFilter                    = THREE.LinearFilter
  var LinearMipMapNearestFilter: TextureFilter       = THREE.LinearMipMapNearestFilter
  var LinearMipMapLinearFilter: TextureFilter        = THREE.LinearMipMapLinearFilter
  var UnsignedByteType: TextureDataType              = THREE.UnsignedByteType
  var ByteType: TextureDataType                      = THREE.ByteType
  var ShortType: TextureDataType                     = THREE.ShortType
  var UnsignedShortType: TextureDataType             = THREE.UnsignedShortType
  var IntType: TextureDataType                       = THREE.IntType
  var UnsignedIntType: TextureDataType               = THREE.UnsignedIntType
  var FloatType: TextureDataType                     = THREE.FloatType
  var UnsignedShort4444Type: PixelType               = THREE.UnsignedShort4444Type
  var UnsignedShort5551Type: PixelType               = THREE.UnsignedShort5551Type
  var UnsignedShort565Type: PixelType                = THREE.UnsignedShort565Type
  var AlphaFormat: PixelFormat                       = THREE.AlphaFormat
  var RGBFormat: PixelFormat                         = THREE.RGBFormat
  var RGBAFormat: PixelFormat                        = THREE.RGBAFormat
  var LuminanceFormat: PixelFormat                   = THREE.LuminanceFormat
  var LuminanceAlphaFormat: PixelFormat              = THREE.LuminanceAlphaFormat
  var RGB_S3TC_DXT1_Format: CompressedPixelFormat    = THREE.RGB_S3TC_DXT1_Format
  var RGBA_S3TC_DXT1_Format: CompressedPixelFormat   = THREE.RGBA_S3TC_DXT1_Format
  var RGBA_S3TC_DXT3_Format: CompressedPixelFormat   = THREE.RGBA_S3TC_DXT3_Format
  var RGBA_S3TC_DXT5_Format: CompressedPixelFormat   = THREE.RGBA_S3TC_DXT5_Format
  var Math: Math                                     = THREE.Math
  var LineStrip: LineType                            = THREE.LineStrip
  var LinePieces: LineType                           = THREE.LinePieces
  var ShaderChunk: ShaderChunk                       = THREE.ShaderChunk
}
