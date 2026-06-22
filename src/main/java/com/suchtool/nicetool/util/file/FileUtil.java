package com.suchtool.nicetool.util.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

@Slf4j
public class FileUtil {

    /**
     * 字节输入流转换为包含二进制数据+文件名称的MultipartFile文件
     *
     * @param inputStream 字节输入流
     * @param fileName    文件名
     */
    public static MultipartFile inputStreamToMultipartFile(InputStream inputStream, String fileName) throws IOException {
        // 该方法可用于测试：return new MockMultipartFile(fileName, fileName, MediaType.MULTIPART_FORM_DATA_VALUE, inputStream);
        FileItem fileItem = createFileItem(inputStream, fileName);
        // CommonsMultipartFile是feign对multipartFile的封装，但是要FileItem类对象
        return new CommonsMultipartFile(fileItem);
    }

    /**
     * FileItem类对象创建
     *
     * @param inputStream inputStream
     * @param fileName    fileName
     * @return FileItem
     */
    public static FileItem createFileItem(InputStream inputStream, String fileName) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        String textFieldName = fileName;
        FileItem item = factory.createItem(textFieldName, MediaType.MULTIPART_FORM_DATA_VALUE, true, fileName);
        int bytesRead = 0;
        byte[] buffer = new byte[10 * 1024 * 1024];
        OutputStream os = null;
        // 使用输出流输出输入流的字节
        try {
            os = item.getOutputStream();
            while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return item;
    }

    /**
     * 将文件导出到响应
     * @param fileContent 文件内容
     * @param fileName 文件名
     */
    public static void exportFileToResponse(byte[] fileContent, String fileName) {
        OutputStream outputStream = assembleResponseOutputStream(fileName);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent);
        try {
            StreamUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 组装响应输出流
     * @param fileName 文件名
     * @return 响应输出流
     */
    public static ServletOutputStream assembleResponseOutputStream(String fileName) {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Assert.notNull(servletRequestAttributes, "RequestAttributes不能为null");

        HttpServletResponse response = servletRequestAttributes.getResponse();
        Assert.notNull(response, "response不能为null");

        String tmpFileName = null;
        try {
            tmpFileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        // 设置响应头信息
        response.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + tmpFileName);
        response.setCharacterEncoding("utf-8");
        // response.setHeader("traceId", NiceLogTraceIdUtil.readTraceId());

        try {
            return response.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
