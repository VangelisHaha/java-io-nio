package nio;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * nio的知识回顾
 *
 * @author Vangelis
 * @date 2019-07-14 15:52
 */

public class NIOTest {

    /**
     *  阻塞模式 客户端
     */
    @Test
    public  void client() throws IOException {
        //获取
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9090));
        //读取文件
        FileChannel fileChannel = FileChannel.open(Paths.get("D:\\code\\java-io-nio\\src\\", "2323.png"), StandardOpenOption.READ);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        while (fileChannel.read(byteBuffer) != -1) {
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            byteBuffer.clear();
        }
        socketChannel.shutdownOutput();
        // 发送完成，接受服务端的反馈
        int len;
        while ((len=socketChannel.read(byteBuffer)) != -1) {
            byteBuffer.flip();
            System.out.println("接受的数据为："+new String(byteBuffer.array(), 0, len));
            byteBuffer.clear();
        }
        fileChannel.close();
        socketChannel.close();
    }

    /**
     *  阻塞 NIO
     *  服务端
     */
    @Test
    public  void server() throws IOException {
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        ssChannel.bind(new InetSocketAddress("127.0.0.1", 9090));
        //获取客户端的连接
        SocketChannel clientChannel = ssChannel.accept();
        FileChannel outChannel = FileChannel.open(Paths.get("D:\\code\\java-io-nio\\src\\", "xixi.png"),
                StandardOpenOption.WRITE,StandardOpenOption.CREATE);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //将文件处理
        while (clientChannel.read(byteBuffer) != -1) {
            byteBuffer.flip();
            outChannel.write(byteBuffer);
            byteBuffer.clear();
        }
        //发送响应
        byteBuffer.put("我收到图片啦，我把这个消息发给你能收到吗？".getBytes());
        byteBuffer.flip();
        clientChannel.write(byteBuffer);
        byteBuffer.clear();
        outChannel.close();
        clientChannel.close();
        ssChannel.close();
    }


    private static void copyFile2() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get("D:\\code\\java-io-nio\\src\\", "2323.png"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("D:\\code\\java-io-nio\\src\\", "2324.png"), StandardOpenOption.READ,StandardOpenOption.WRITE,StandardOpenOption.CREATE);
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inChannel.close();
        outChannel.close();
    }

    /**
     *  使用NIO进行文件复制
     *  直接内存版本
     */
    private static void copyFile() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get("D:\\code\\java-io-nio\\src\\", "2323.png"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("D:\\code\\java-io-nio\\src\\", "2324.png"), StandardOpenOption.READ,StandardOpenOption.WRITE,StandardOpenOption.CREATE);
        MappedByteBuffer readMap = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        MappedByteBuffer writeMap = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());
        // 使用数组传递
        byte[] bytes = new byte[readMap.limit()];
        readMap.get(bytes);
        writeMap.put(bytes);
        inChannel.close();
        outChannel.close();
    }


    /**
     *  使用NIO进行文件复制
     *  非直接缓冲区 版本
     */
    private static void indirectCopyFile(){
            // 思路，创建输入输出流，然后从流获取通道，创建缓冲区，
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            fileInputStream = new FileInputStream("D:\\code\\java-io-nio\\src\\2323.png");
            fileOutputStream = new FileOutputStream("D:\\code\\java-io-nio\\src\\2323_copy.png");
            //获取通道
            inChannel = fileInputStream.getChannel();
            outChannel = fileOutputStream.getChannel();
            //创建缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            //从通道中读取到缓冲区
            while (inChannel.read(buffer) != -1) {
                //将缓冲区数据放入out通道中，切换到buffer的读模式
                buffer.flip();
                outChannel.write(buffer);
                //清空缓冲区域
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //各种恶心的关闭操作i
            try {
                if (inChannel != null) {
                    inChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (outChannel != null) {
                    outChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
