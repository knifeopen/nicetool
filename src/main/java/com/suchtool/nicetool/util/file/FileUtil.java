package com.suchtool.nicetool.util.file;

import com.suchtool.nicetool.util.file.vo.FileNameExtVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Slf4j
public class FileUtil {

    private static final String DEFAULT_FIELD_NAME = "file";

    private static final int DISK_FILE_ITEM_FACTORY_SIZE_THRESHOLD = 10240;

    /**
     * 字节输入流转换为包含二进制数据+文件名称的MultipartFile文件
     *
     * @param inputStream 字节输入流
     * @param fileName    文件名
     */
    public static MultipartFile toMultipartFile(InputStream inputStream, String fileName) {
        FileItem fileItem = createFileItem(inputStream, fileName);
        if (fileItem == null) {
            throw new RuntimeException("创建FileItem失败");
        }
        return new CommonsMultipartFile(fileItem);
    }

    /**
     * 解析文件名和扩展名，无扩展名时扩展名为空字符串
     *
     * @param fileName 完整文件名
     */
    public static FileNameExtVO parseFileNameAndExt(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new RuntimeException("文件名不能为空");
        }

        String tmpFileName = "";
        String tmpExtName = "";
        int index = fileName.lastIndexOf('.');
        if (index < 0) {
            tmpFileName = StringUtils.trimWhitespace(fileName);
        } else if (index == 0) {
            tmpFileName = "";
            tmpExtName = StringUtils.trimWhitespace(fileName.substring(1));
        } else {
            tmpFileName = StringUtils.trimWhitespace(fileName.substring(0, index));
            tmpExtName = StringUtils.trimWhitespace(fileName.substring(index + 1));
        }

        FileNameExtVO fileNameExtVO = new FileNameExtVO();
        fileNameExtVO.setFileName(tmpFileName);
        fileNameExtVO.setExtName(tmpExtName);
        return fileNameExtVO;
    }

    /**
     * FileItem类对象创建
     *
     * @param inputStream 输入流
     * @param fileName    文件名
     * @return FileItem
     */
    public static FileItem createFileItem(InputStream inputStream, String fileName) {
        return createFileItem(inputStream, DEFAULT_FIELD_NAME, fileName);
    }

    private static FileItem createFileItem(InputStream inputStream, String fieldName, String fileName) {
        DiskFileItemFactory factory = new DiskFileItemFactory(DISK_FILE_ITEM_FACTORY_SIZE_THRESHOLD, null);
        FileItem fileItem = factory.createItem(fieldName, MediaType.MULTIPART_FORM_DATA_VALUE, true, fileName);
        OutputStream outputStream = null;
        try {
            outputStream = fileItem.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new RuntimeException("写入FileItem异常", e);
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(inputStream);
        }
        return fileItem;
    }

    /**
     * 将文件导出到响应
     *
     * @param fileContent 文件内容
     * @param fileName    文件名
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
     * 组装响应
     *
     * @param fileName 文件名
     * @return 响应输出流
     */
    public static HttpServletResponse assembleResponse(String fileName) {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Assert.notNull(servletRequestAttributes, "RequestAttributes不能为null");

        HttpServletResponse response = servletRequestAttributes.getResponse();
        Assert.notNull(response, "response不能为null");

        String tmpFileName;
        try {
            tmpFileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        response.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + tmpFileName);
        response.setCharacterEncoding("utf-8");

        return response;
    }

    /**
     * 组装响应输出流
     *
     * @param fileName 文件名
     * @return 响应输出流
     */
    public static ServletOutputStream assembleResponseOutputStream(String fileName) {
        try {
            return assembleResponse(fileName).getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
