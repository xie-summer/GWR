package test;

import org.im4java.core.CompositeCmd;
import org.im4java.core.IMOperation;

import com.gewara.util.PictureUtil;
public class PictureTest {
	public static boolean compose(String over, String background, String dest){
		CompositeCmd convert = new CompositeCmd();
		IMOperation op = new IMOperation();
		//op.composite();
		op.addImage(background, over);
		//op.compose("over");
		//op.gravity("center");
		op.addRawArgs("-geometry", "+20+20");
		//op.geometry(20, 20);
		op.addImage(dest);
		try {
			System.out.println(op.toString());
			System.out.println(convert.toString());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public static void main(String[] args) throws Exception{
		System.out.print(PictureUtil.resize("F:\\a.jpg", "F:\\x.jpg", 100, 100));
		/*int i=10;
		System.out.println("xxxx"+i);
		System.out.print(Boolean.FALSE.compareTo(Boolean.TRUE));
		compose("F:\\a.jpg", "F:\\b.jpg", "F:\\c.jpg");*/
	}
}
