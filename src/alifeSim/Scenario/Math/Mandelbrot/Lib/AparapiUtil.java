package alifeSim.Scenario.Math.Mandelbrot.Lib;
import com.amd.aparapi.OpenCLDevice;
import com.amd.aparapi.OpenCLPlatform;

public class AparapiUtil
{
	public static OpenCLDevice chooseOpenCLDevice()
	{
		OpenCLDevice dev = OpenCLDevice.select(new OpenCLDevice.DeviceSelector()
		{
			public OpenCLDevice select(OpenCLDevice d)
			{
				// System.out.println("Devices " +
				// d.getPlatform().getDevices());

				OpenCLDevice intel = null;
				OpenCLDevice amd = null;
				OpenCLDevice nvidia = null;

				/* Will choice a device in this order of preference -
				 * AMD GPU, nVidia GPU , Intel GPU, Intel CPU, AMD CPU
				 */
				for (OpenCLPlatform platform : d.getPlatform().getPlatforms())
				{

					if (platform.getName().contains("AMD"))
					{
						for (OpenCLDevice dev : platform.getDevices())
						{
							if (dev.getType().toString().equals("GPU"))
							{
								System.out.println("AMD GPU Detected");

								amd = dev;
								
								break;
							}
						}
					}

					if (platform.getName().contains("NVIDIA"))
					{
						for (OpenCLDevice dev : platform.getDevices())
						{
							if (dev.getType().toString().equals("GPU"))
							{
								System.out.println("NVIDIA GPU Detected");

								nvidia = dev;
								
								break;
							}
						}
					}

					if (platform.getName().contains("Intel"))
					{
						for (OpenCLDevice dev : platform.getDevices())
						{
							if (dev.getType().toString().equals("GPU"))
							{
								System.out.println("Intel GPU Detected");

								intel = dev;
								
								break;
							}
						}
					}
					
					if (platform.getName().contains("Intel"))
					{
						for (OpenCLDevice dev : platform.getDevices())
						{
							if (dev.getType().toString().equals("CPU"))
							{
								System.out.println("Intel CPU Detected");

								intel = dev;
								
								break;
							}
						}
					}
					
					if (platform.getName().contains("AMD"))
					{
						for (OpenCLDevice dev : platform.getDevices())
						{
							if (dev.getType().toString().equals("CPU"))
							{
								System.out.println("AMD CPU Detected");

								amd = dev;
								
								break;
							}
						}
					}
				}

				if (amd != null)
				{
					System.out.println("Selected : " + amd.getPlatform().getName() + " " + amd.getType());

					return amd;
				}

				if (nvidia != null)
				{
					System.out.println("Selected : " + nvidia.getPlatform().getName() + " " + nvidia.getType());

					return nvidia;
				}

				if (intel != null)
				{
					System.out.println("Selected : " + intel.getPlatform().getName() + " " + intel.getType());

					return intel;
				}

				return null;
			}
		});

		return dev;
	}
}
