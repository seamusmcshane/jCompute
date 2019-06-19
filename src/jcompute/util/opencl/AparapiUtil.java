package jcompute.util.opencl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amd.aparapi.device.OpenCLDevice;
import com.amd.aparapi.internal.opencl.OpenCLPlatform;

public class AparapiUtil
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(AparapiUtil.class);

	public static OpenCLDevice chooseOpenCLDevice()
	{
		OpenCLDevice dev = OpenCLDevice.select(new OpenCLDevice.DeviceSelector()
		{
			@Override
			public OpenCLDevice select(OpenCLDevice d)
			{
				OpenCLDevice intel = null;
				OpenCLDevice amd = null;
				OpenCLDevice nvidia = null;

				/*
				 * Will choice a device in this order of preference - AMD GPU,
				 * nVidia GPU , Intel GPU, Intel CPU, AMD CPU
				 */
				log.info("-------------------");
				log.info("Platform List");
				log.info("-------------------");
				for(OpenCLPlatform platform : d.getOpenCLPlatform().getOpenCLPlatforms())
				{
					log.info(platform.getName());
				}
				log.info("-------------------");
				log.info("Device List");
				log.info("-------------------");
				for(OpenCLPlatform platform : d.getOpenCLPlatform().getOpenCLPlatforms())
				{
					
					if(platform.getName().contains("AMD"))
					{
						for(OpenCLDevice dev : platform.getOpenCLDevices())
						{
							if(dev.getType().toString().equals("GPU"))
							{
								log.info("AMD GPU Detected");

								amd = dev;

								break;
							}
						}
					}

					if(platform.getName().contains("NVIDIA"))
					{
						for(OpenCLDevice dev : platform.getOpenCLDevices())
						{
							if(dev.getType().toString().equals("GPU"))
							{
								log.info("NVIDIA GPU Detected");

								nvidia = dev;

								break;
							}
						}
					}

					if(platform.getName().contains("Intel"))
					{
						for(OpenCLDevice dev : platform.getOpenCLDevices())
						{
							if(dev.getType().toString().equals("GPU"))
							{
								log.info("Intel GPU Detected");

								intel = dev;

								break;
							}
						}
					}

					if(intel == null)
					{
						if(platform.getName().contains("Intel"))
						{
							for(OpenCLDevice dev : platform.getOpenCLDevices())
							{
								if(dev.getType().toString().equals("CPU"))
								{
									log.info("Intel CPU Detected");

									intel = dev;

									break;
								}
							}
						}
					}

					if(amd == null)
					{
						if(platform.getName().contains("AMD"))
						{
							for(OpenCLDevice dev : platform.getOpenCLDevices())
							{
								if(dev.getType().toString().equals("CPU"))
								{
									log.info("AMD CPU Detected");

									amd = dev;

									break;
								}
							}
						}
					}

				}
				log.info("-------------------");

				if(amd != null)
				{
					log.info("Selected : " + amd.getOpenCLPlatform().getName() + " " + amd.getType());
					log.info("-------------------");

					return amd;
				}

				if(nvidia != null)
				{
					log.info("Selected : " + nvidia.getOpenCLPlatform().getName() + " " + nvidia.getType());
					log.info("-------------------");

					return nvidia;
				}

				if(intel != null)
				{
					log.info("Selected : " + intel.getOpenCLPlatform().getName() + " " + intel.getType());
					log.info("-------------------");

					return intel;
				}

				log.error("No device Selected");

				return null;
			}
		});

		return dev;
	}

	public static OpenCLDevice selectDevByVendorAndType(final String gpuVendor, final String type)
	{
		OpenCLDevice dev = OpenCLDevice.select(new OpenCLDevice.DeviceSelector()
		{
			@Override
			public OpenCLDevice select(OpenCLDevice d)
			{
				for(OpenCLPlatform platform : d.getOpenCLPlatform().getOpenCLPlatforms())
				{
					if(platform.getName().toLowerCase().contains(gpuVendor.toLowerCase()))
					{
						for(OpenCLDevice dev : platform.getOpenCLDevices())
						{
							if(dev.getType().toString().toLowerCase().contains(type.toLowerCase()))
							{
								log.info(gpuVendor + " " + type + " Detected");

								return dev;
							}
						}
					}
				}

				log.error("No device detected");

				return null;
			}
		});

		return dev;
	}
}
