package com.project.milo.common.exception.file;

/**
 * 文件名大小限制异常类
 * 
 * @author ruoyi
 */
public class FileSizeLimitExceededException extends com.project.milo.common.exception.file.FileException
{
    private static final long serialVersionUID = 1L;

    public FileSizeLimitExceededException(long defaultMaxSize)
    {
        super("upload.exceed.maxSize", new Object[] { defaultMaxSize });
    }
}
