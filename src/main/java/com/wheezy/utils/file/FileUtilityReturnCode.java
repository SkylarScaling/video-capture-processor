package com.wheezy.utils.file;

/**
 * Return codes with informational messages for file utility methods 
 */
public enum FileUtilityReturnCode
{
  SUCCESS("Success"),
  FILE_DOES_NOT_EXIST("File does not exist"),
  DIRECTORY_CREATION_ERROR("Directory creation error"),
  FILE_MOVE_ERROR("File move error"),
  INVALID_COPY_LOCATION("Invalid copy location"),
  DIRECTORY_DELETE_ERROR("Directory delete error"),
  FILE_DELETE_ERROR("File delete error");

  private String message;

  private FileUtilityReturnCode(String message)
  {
    this.message = message;
  }

  public String getMessage()
  {
    return message;
  }
}
