package com.lwj.socket.server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DecimalFormat;

/**
 * socket服务端
 * @author lwj
 */
public class SocketServer extends ServerSocket{

    public SocketServer() throws IOException{
        super(Integer.parseInt(SERVER_PORT));
    }
    private static String performance = "1,2,3";
    private static String timeout = "60000";
    private static String size = "1024";
    private static String SERVER_PORT = "9804";
    private static DecimalFormat df = null;
    private static String tcpNoDelay = "true";
    private static String filepath = "C:\\JavaScoket";
    static{
        df =  new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(1);
    }

    public void load() {
        while(true){
            try {
                System.out.println("=======服务端已启动=======");
                Socket socket = this.accept();
                new Thread(new Task(socket)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class Task implements Runnable {
        private Socket socket;
        private DataInputStream dis;
        private FileOutputStream fos;

        public Task(Socket socket) {
            this.socket = socket;
            try {
                boolean flag = true;
                if ("false".equalsIgnoreCase(tcpNoDelay)) {
                    flag = false;
                }
                this.socket.setTcpNoDelay(flag);
                this.socket.setReceiveBufferSize(Integer.parseInt(size));
                this.socket.setSoTimeout(Integer.parseInt(timeout));
                String[] split = performance.split(",");
                this.socket.setPerformancePreferences(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                dis = new DataInputStream(socket.getInputStream());
                String filename = dis.readUTF();
                long filelength = dis.readLong();
                File directory = new File(filepath);
                if (!directory.exists()) {
                    directory.mkdir();
                } else {
                    directory.deleteOnExit();
                }
                File file = new File(directory.getAbsolutePath()+File.separatorChar+filename);
                fos = new FileOutputStream(file);
                long start = System.currentTimeMillis();
                byte[] bytes = new byte[1024];
                int length = 0;
                while ((length = dis.read(bytes,0,bytes.length))!=-1) {
                    fos.write(bytes,0,length);
                    fos.flush();
                }
                long end = System.currentTimeMillis();

                System.out.println("========文件接受成功："+filename+"\t文件大小："+getFormatFileSize(filelength)+"========");
                System.out.println("========文件接收时间："+((end-start)/1000)+"."+((end-start)%1000)+"s"+"========");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos!=null) {
                        fos.close();
                    }
                    if (dis!=null) {
                        dis.close();
                    }
                    if (socket!=null) {
                        socket.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    private String getFormatFileSize(long length){
        double size = ((double) length) / (1 << 30);
        if (size >= 1) {
            return df.format(size)+"GB";
        }
        size = ((double) length) / (1 << 20);
        if (size >= 1) {
            return df.format(size)+"MB";
        }
        size = ((double) length) / (1 << 10);
        if (size >= 1) {
            return df.format(size)+"KB";
        }
        return length+"Byte";
    }


    public static void main(String[] args) {
        if (args!=null) {
            if (args.length>0) {
                SERVER_PORT = args[0];
            }
            if (args.length>1) {
                filepath = args[1];
            }
            if (args.length>2) {
                tcpNoDelay = args[2];
            }
            if (args.length>3) {
                performance = args[3];
            }
            if (args.length>4) {
                timeout = args[4];
            }
            if (args.length>5) {
                size = args[5];
            }
        }
        try {
            SocketServer server = new SocketServer();
            server.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
