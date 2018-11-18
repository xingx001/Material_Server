import com.simple.springbootbasic.utils.RequestUtils;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * @author simple
 * @description TODO
 * @date 2018/11/13 11:43
 */
public class ApplicationTest {
    public static void main(String[] args) throws Exception {
        String jpg = RequestUtils.downResource("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=854116639,3698237402&fm=200&gp=0.jpg", "/Users/simple/work", ".jpg");
        Magic parser = new Magic() ;
        File file = new File("/Users/simple/work/" + jpg);
        MagicMatch match = (MagicMatch) parser.getMagicMatch(FileUtils.readFileToByteArray(file));
        String uuid=jpg.substring(0,jpg.lastIndexOf("."));
        String newType=match.getMimeType().substring(match.getMimeType().lastIndexOf("/")+1);
        String fileName=uuid+"."+newType;
        //重命名
        file.renameTo(new File("/Users/simple/work/"+fileName));

    }
}
