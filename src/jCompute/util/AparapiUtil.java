package jCompute.util;

import com.amd.aparapi.device.OpenCLDevice;
import com.amd.aparapi.internal.opencl.OpenCLPlatform;

public class AparapiUtil
{
	public static OpenCLDevice chooseOpenCLDevice()
	{
		OpenCLDevice dev = OpenCLDevice.select(new OpenCLDevice.DeviceSelector()
		{
			public OpenCLDevice select(OpenCLDevice d)
			{
				OpenCLDevice intel = null;
				OpenCLDevice amd = null;
				OpenCLDevice nvidia = null;

				/*
				 * Will choice a device in this order of preference - AMD GPU,
				 * nVidia GPU , Intel GPU, Intel CPU, AMD CPU
				 */
				System.out.println("-------------------");
				System.out.println("Platform List");
				System.out.println("-------------------");
				for(OpenCLPlatform platform : d.getOpenCLPlatform().getOpenCLPlatforms())
				{
					System.out.println(platform.getName());
				}
				System.out.println("-------------------");
				System.out.println("Device List");
				System.out.println("-------------------");
				for(OpenCLPlatform platform : d.getOpenCLPlatform().getOpenCLPlatforms())
				{

					if(platform.getName().contains("AMD"))
					{
						for(OpenCLDevice dev : platform.getOpenCLDevices())
						{
							if(dev.getType().toString().equals("GPU"))
							{
								System.out.println("AMD GPU Detected");

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
								System.out.println("NVIDIA GPU Detected");

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
								System.out.println("Intel GPU Detected");

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
									System.out.println("Intel CPU Detected");

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
									System.out.println("AMD CPU Detected");

									amd = dev;

									break;
								}
							}
						}
					}

				}
				System.out.println("-------------------");

				if(amd != null)
				{
					System.out.println("Selected : " + amd.getOpenCLPlatform().getName() + " " + amd.getType());
					System.out.println("-------------------");

					return amd;
				}

				if(nvidia != null)
				{
					System.out.println("Selected : " + nvidia.getOpenCLPlatform().getName() + " " + nvidia.getType());
					System.out.println("-------------------");

					return nvidia;
				}

				if(intel != null)
				{
					System.out.println("Selected : " + intel.getOpenCLPlatform().getName() + " " + intel.getType());
					System.out.println("-------------------");

					return intel;
				}

				return null;
			}
		});

		return dev;
	}

	public static OpenCLDevice selectDevByVendorAndType(final String gpuVendor, final String type)
	{
		OpenCLDevice dev = OpenCLDevice.select(new OpenCLDevice.DeviceSelector()
		{
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
								System.out.println(gpuVendor + " " + type + " Detected");

								return dev;
							}
						}
					}
				}
				return null;
			}
		});

		return dev;
	}
}
