package com.dongqiao.seckill.exception;

public class ShopException extends Exception
{
	public ShopException(String message)
	{
		super(message);
	}
	
	public ShopException(String message, Throwable cause)
	{
		super(message,cause);
	}
}