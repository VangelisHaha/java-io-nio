package nio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 非阻塞的NIO 重点重点重点重点重点重点重点重点重点重点重点
 * 学NIO的目的
 *
 * @author Vangelis
 * @date 2019-07-14 21:03
 */

public class TestNonBlockingNIO {

    private static String ip = "127.0.0.1";
    private static int port = 9898;
    private static SocketAddress localAddress = new InetSocketAddress(ip, port);

    /**
     * 客户端
     */
    @Test
    public void client() throws IOException {
        // 获取客户端
        SocketChannel sChannel = SocketChannel.open(localAddress);
        // 切换成非阻塞模式
        sChannel.configureBlocking(false);
        // 分配指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put("我叫你一声你敢答应吗？".getBytes());
        buf.flip();
        //发送出去
        sChannel.write(buf);
        buf.clear();
        //关闭通道
        sChannel.close();
    }

    /**
     * 服务端
     */
    @Test
    public void server() throws IOException {
        //获取服务端
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        //设置为非阻塞模式
        ssChannel.configureBlocking(false);
        // 绑定连接
        ssChannel.bind(localAddress);
        // 获取选择器
        Selector selector = Selector.open();
        // 将通道注册到选择器
        ssChannel.register(selector, SelectionKey.OP_ACCEPT);
        // 轮询式的获取选择器上已经 “准备就绪”的事件
        while (selector.select() > 0) {
            //表示有一个准备就绪
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectionKeys.iterator();
            while (it.hasNext()) {
                SelectionKey next = it.next();
                // 获取准备就绪的事件
                // 如果已经连接准备就绪
                if (next.isAcceptable()) {
                    // 获取客户端的连接
                    SocketChannel sChannel = ssChannel.accept();
                    // 客户端连接也需要切换成非阻塞
                    sChannel.configureBlocking(false);
                    // 将客户端也注册到选择器上
                    sChannel.register(selector, SelectionKey.OP_READ);
                }
                //对应读的准备就绪
                 if (next.isReadable()) {
                    SocketChannel sChannel = (SocketChannel) next.channel();
                    // 创建缓冲区
                    ByteBuffer buf = ByteBuffer.allocate(1024);
                    int len;
                    while ((len = sChannel.read(buf)) != -1) {
                        buf.flip();
                        System.out.println("我是服务端：我接受的信息为:" + new String(buf.array(), 0, len));
                        buf.clear();
                    }
                }
                // 用完的key需要删除
                it.remove();
            }

        }


    }
}
