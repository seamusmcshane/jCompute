package jcompute.results.custom;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.batch.log.item.custom.logger.CustomCSVItemLogFormatInf;

public class CustomItemResultParser
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(CustomItemResultParser.class);
	
	/**
	 * Conversion from Object to bytes (data placed in field order).
	 */
	public static byte[] CustomItemResultToBytes(CustomCSVItemLogFormatInf customItemResult)
	{
		int numberOfFields = customItemResult.numberOfFields();
		
		/*
		 * ***************************************************************************************************
		 * Buffer Size Calculation (Pass1)
		 *****************************************************************************************************/
		
		int ByteBufferSize = 0;
		
		for(int f = 0; f < numberOfFields; f++)
		{
			// Field
			CustomResultFieldType type = customItemResult.getFieldType(f);
			
			Object obj = customItemResult.getFieldValue(f);
			
			// Field Type
			ByteBufferSize += 4;
			
			switch(type)
			{
				case Integer:
				{
					// Value
					ByteBufferSize += 4;
				}
				break;
				case Double:
				{
					// Value
					ByteBufferSize += 8;
				}
				break;
				case Float:
				{
					// Value
					ByteBufferSize += 4;
				}
				break;
				case Long:
				{
					// Value
					ByteBufferSize += 8;
				}
				break;
				case String:
				{
					String objS = (String) obj;
					
					// Len + Value
					ByteBufferSize += (4 + objS.getBytes().length);
				}
				break;
				case Boolean:
				{
					// Value
					ByteBufferSize += 4;
				}
				break;
				case Unsupported:
					log.error("Field at index " + f + " Unsupported");
				break;
			}
			
		}
		
		/*
		 * ***************************************************************************************************
		 * Field Storage
		 *****************************************************************************************************/
		
		// Now we have calculated the length, allocate a byte buffer
		ByteBuffer tbuffer = ByteBuffer.allocate(ByteBufferSize);
		
		for(int f = 0; f < numberOfFields; f++)
		{
			// Field
			CustomResultFieldType type = customItemResult.getFieldType(f);
			
			Object obj = customItemResult.getFieldValue(f);
			
			// Field Type
			tbuffer.putInt(type.index);
			
			switch(type)
			{
				case Integer:
				{
					int val = (int) obj;
					
					tbuffer.putInt(val);
				}
				break;
				case Double:
				{
					double val = (double) obj;
					
					tbuffer.putDouble(val);
				}
				break;
				case Float:
				{
					float val = (float) obj;
					
					tbuffer.putFloat(val);
				}
				break;
				case Long:
				{
					long val = (long) obj;
					
					tbuffer.putLong(val);
				}
				break;
				case String:
				{
					// Type
					byte[] val = ((String) obj).getBytes();
					
					int slen = val.length;
					
					// SLen + SValue
					tbuffer.putInt(slen);
					tbuffer.put(val);
				}
				break;
				case Boolean:
				{
					boolean bval = (boolean) obj;
					
					int val = (bval ? 0 : 1);
					
					tbuffer.putInt(val);
				}
				break;
				case Unsupported:
					// Network will fail here - this is a bug - Unsupported is never sent.
					log.error("Field at index " + f + " Unsupported");
				break;
			}
		}
		
		// The backing byte array
		return tbuffer.array();
	}
	
	/**
	 * Exact Reverse of Conversion from bytes to object (data read in field order).
	 */
	public static void BytesToRow(byte[] bytes, CustomCSVItemLogFormatInf destination)
	{
		ByteBuffer tbuffer = ByteBuffer.wrap(bytes);
		
		int numberOfFields = destination.numberOfFields();
		
		for(int f = 0; f < numberOfFields; f++)
		{
			int fieldTypeIndex = tbuffer.getInt();
			
			// Field
			Object obj = destination.getFieldValue(f);
			
			// Supported Type
			CustomResultFieldType type = CustomResultFieldType.fromInt(fieldTypeIndex);
			
			log.debug("f " + f + " " + String.valueOf(obj) + " type " + type.index + " fieldTypeIndex " + fieldTypeIndex);
			
			switch(type)
			{
				case Integer:
				{
					int val = tbuffer.getInt();
					
					destination.setFieldValue(f, val);
				}
				break;
				case Double:
				{
					double val = tbuffer.getDouble();
					
					destination.setFieldValue(f, val);
				}
				break;
				case Float:
				{
					float val = tbuffer.getFloat();
					
					destination.setFieldValue(f, val);
				}
				break;
				case Long:
				{
					long val = tbuffer.getLong();
					
					destination.setFieldValue(f, val);
				}
				break;
				case String:
				{
					int len = tbuffer.getInt();
					
					byte[] dst = new byte[len];
					
					tbuffer.get(dst, 0, len);
					
					String val = new String(dst);
					
					destination.setFieldValue(f, val);
				}
				break;
				case Boolean:
				{
					int ival = tbuffer.getInt();
					
					boolean val = (ival == 0 ? true : false);
					
					destination.setFieldValue(f, val);
				}
				break;
				case Unsupported:
				// Unsupported
				break;
			}
		}
	}
}
