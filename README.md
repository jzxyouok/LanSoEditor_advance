# LanSoEditor_advance
android  video editor  advance sdk . filter, overlay,remark,mixer and so on安卓平台视频编辑高级版本，可以滤镜，叠加，标记等操作

###当前版本是LanSoEditor_advance1.6 视频编辑的高级版本
*  基本覆盖了秒拍,美拍,快手等视频编辑的大部分功能.
*  增加了44种滤镜,基本覆盖市面上大部分APP中的滤镜效果.
*  可以实现视频和视频, 视频和图片,视频和您的UI界面叠加.
*  在叠加的过程中:支持任意时间点的加入,隐藏,显示,退出.支持同时获取媒体来任意叠加,支持叠加过程中的各种调节,支持实时保存.
*  可以实现 图片和图片的叠加,来实现多张图片合并成影集的效果.
*  可以实现当视频播放中,手指滑动画面,即出现一个箭头,来实时的标注.后期我们会举例涂鸦的功能.
*  支持声音混合,音量调节.
*  我们完全以API的形式呈现,稳定可靠,简单易用,您可以根据项目的个性化而任意的发挥.


###核心架构
*  我们设计了ISprite类,可以实现旋转,缩放,平移,RGBA值的调节,隐藏/显示等功能,您可以认为类似Android的各种控件继承自View一样使用.
*
*  我们设计了MediaPool架构, 你可以像ThreadPool,android中的Handler一样使用它.获取一个ISprite,释放一个ISprite,是一个"媒体池"
*  
*  当前继承ISprite的有:VideoSprite,BitmapSprite,FilterSprite,ViewSprite; 
*  VideoSprite: 处理视频画面,可以从MediaPool"媒体池"中获取多个,从中得到surface,设置到您的播放器中,然后在播放过程中进行各种编辑功能,
								比如您可以同时获取两个VideoSprite,一个用来显示,另一个把透明度调整为0来叠加,实现透明叠加的效果								
								
*  FilterSprite: 处理视频滤镜,同VideoSprite一样使用,并支持44种视频滤镜,您可以在视频播放中,任意的更换滤镜效果,
									也可以在滤镜过程中增加另外的ISprite,一起实现您的个性化效果.
									
*  BitmapSprite: 处理图片画面,可以从MediaPool"媒体池"中获取多个,可以单独使用,来生成照片影集,也可以和别的ISprite混合使用,呈现花样的效果.

*  ViewSprite  : 处理您设计的UI,比如你可以关联一个TextView,把TextView上的文字加到视频中,也可以关联一个您设计好的炫酷的UI效果,
								比如一个LinearLayout,一个RelativeLayout等等.							来合成视频,这个我们后期会陆续的增加各种举例,当然您也可以自由发挥.			
													
*  此SDK采用为收费授权,公司性质的合作,为了您项目更好的进行,欢迎和我们联系.谢谢!

###可给我们发邮件,获取测试机型报告.

###下载地址: 
*  https://github.com/LanSoSdk/LanSoEditor_advance

###我们有基本视频编辑,以方便您项目中基本需求：
*	https://github.com/LanSoSdk/LanSoEditor_common


###直接下载获取APK:
   下载整个项目后, 在bin文件下有apk, 直接安装后即可演示.


###联系方式:
*   QQ 1852600324 
*   Email:support@lansongtech.com
