package com.project.milo.common.exception.file;

/**
 * 文件名称超长限制异常类
 * 
 * @author ruoyi
 */
public class FileNameLengthLimitExceededException extends com.project.milo.common.exception.file.FileException
{
    private static final long serialVersionUID = 1L;

    public FileNameLengthLimitExceededException(int defaultFileNameLength)
    {
        super("upload.filename.exceed.length", new Object[] { defaultFileNameLength });
    }
}
