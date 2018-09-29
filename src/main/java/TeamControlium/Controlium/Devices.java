package TeamControlium.Controlium;

import TeamControlium.Utilities.Logger;
import TeamControlium.Utilities.TestData;

public class Devices {

    private static final String[] ConfigDevice = {"Selenium", "Device"};

    /// <summary>
    /// Supported Devices.  Device is set when Driver is instantiated.  Driver gets the Device to be used from the run configuration
    /// option "Device" in category "General".  Windows Vista was considered a joke and is not supported by Cloud providers (at this time or writing).  iPhones
    /// may have a number of different iOS versions installed and so it is the iOS version that is set; see the Devices for the assumed host iPhone.
    /// <para/><para/>
    /// When running remote Selenium the browser/version is used in the Desired Capabilities to request
    /// that device (or device simulator).
    /// <para/><para/>
    /// When running locally the selected device is ignored and Windows 7 assumed.
    /// </summary>
    public enum DeviceType {
        NoneSelected,
        WinXP,
        Win7,
        Win8,
        Win8_1,
        Win10,
        iPadPro,
        iPad,
        iPad2,
        iPad3,
        iPad4,
        iPad2017,
        iPad2018,
        iPadMini,
        iPadMini2,
        iPadMini3,
        iPadMini4,
        iPadAir,
        iPadAir2,
        iOS3,
        iOS4,
        iOS5,
        iOS6,
        iOS7,
        iOS9,
        iOS10,
        iOS11,
        GalaxyS2,
        GalaxyS3,
        GalaxyS4,
        GalaxyS5,
        GalaxyS6,
        GalaxyS7,
        GalaxyS8,
        GalaxyS9
    }

    /// <summary>
    /// Device hosting browser Selenium script executing against
    /// </summary>
    /// <seealso cref="Devices">Lists all possible Devices that can be returned.</seealso>
    private static DeviceType _TestDevice;

    public static DeviceType getDeviceType() {
        return _TestDevice;
    }

    public static void SetTestDevice() { SetTestDevice(null);}
    public static void SetTestDevice(String device) {
        String deviceNormalized;

        try {
            String deviceFromTestData = TestData.getItem(String.class, ConfigDevice[0], ConfigDevice[1]);
            if (!deviceFromTestData.isEmpty()) {
                if (device!=null && !device.isEmpty()) {
                    Logger.WriteLine(Logger.LogLevels.TestInformation, String.format("Device set in Test Data.  Overriding passed device with [%s] (Test data [%s.%s])",deviceFromTestData, ConfigDevice[0], ConfigDevice[1]));
                }
                device = deviceFromTestData;
            }
        } catch (Exception e) {
            if (device==null || device.isEmpty()) {
                Logger.WriteLine(Logger.LogLevels.Error, String.format("Cannot get Device type setting from test data: [%s],[%s]. Defaulting to Windows", ConfigDevice[0], ConfigDevice[1]));
                device = "Windows";
            }
        }


        if (device == null || device.isEmpty()) {
            Logger.WriteLine(Logger.LogLevels.Error, String.format("Empty Device type in test data: [%s],[%s].", ConfigDevice[0], ConfigDevice[1]));
            throw new RuntimeException(String.format("Empty Device type in test data: [%s],[%s].", ConfigDevice[0], ConfigDevice[1]));
        }

        deviceNormalized = device.toLowerCase().replace(" ", "").replace("(", "").replace(")", "").replace("-", "");

        // If a Galaxy s6..... Save a million cases
        if (deviceNormalized.startsWith("smg920")) deviceNormalized = "galaxys6";

        // If a Galaxy s7..... Save a million cases
        if (deviceNormalized.startsWith("smg930")) deviceNormalized = "galaxys7";

        switch (deviceNormalized) {
            case "xp":
            case "windowsxp":
                _TestDevice = DeviceType.WinXP;
                break;
            case "windows7":
            case "win7":
            case "w7":
                _TestDevice = DeviceType.Win7;
                break;
            case "windows8":
            case "win8":
            case "w8":
            case "windows8.0":
            case "win8.0":
            case "w8.0":
            case "windows80":
            case "win80":
            case "w80":
            case "windows8_0":
            case "win8_0":
            case "w8_0":
                _TestDevice = DeviceType.Win8;
                break;
            case "windows8.1":
            case "win8.1":
            case "w8.1":
            case "windows81":
            case "win81":
            case "w81":
            case "windows8_1":
            case "win8_1":
            case "w8_1":
                _TestDevice = DeviceType.Win8_1;
                break;
            case "windows10":
            case "win10":
            case "w10":
            case "windows":
                _TestDevice = DeviceType.Win10;
                break;
            case "ipadpro":
            case "a1670":
                _TestDevice = DeviceType.iPadPro;
                break;
            case "ipad":
            case "ipad1":
            case "a1219":
            case "a1337":
                _TestDevice = DeviceType.iPad;
                break;
            case "ipad2":
            case "a1395":
            case "a1396":
            case "a1397":
                _TestDevice = DeviceType.iPad2;
                break;
            case "ipad3":
            case "ipad3rdgen":
            case "ipad3rdgeneration":
            case "a1416":
            case "a1430":
            case "a1403":
                _TestDevice = DeviceType.iPad3;
                break;
            case "ipad4":
            case "ipad4thgen":
            case "ipad4thgeneration":
            case "a1458":
            case "a1459":
            case "a1460":
                _TestDevice = DeviceType.iPad4;
                break;
            case "ipad2017":
            case "a1822":
            case "a1823":
                _TestDevice = DeviceType.iPad2017;
                break;
            case "ipad2018":
                _TestDevice = DeviceType.iPad2018;
                break;
            case "ipadmini":
            case "ipadmini1":
            case "a1432":
            case "a1454":
            case "a1455":
                _TestDevice = DeviceType.iPadMini;
                break;
            case "ipadm2":
            case "ipadmini2":
            case "a1489":
            case "a1490":
            case "a1491":
                _TestDevice = DeviceType.iPadMini2;
                break;
            case "ipadm3":
            case "ipadmini3":
            case "a1599":
            case "a1600":
                _TestDevice = DeviceType.iPadMini3;
                break;
            case "ipadm4":
            case "ipadmini4":
            case "a1538":
            case "a1550":
                _TestDevice = DeviceType.iPadMini4;
                break;
            case "ipadair":
            case "ipadair1":
            case "a1474":
            case "a1475":
            case "a1476":
                _TestDevice = DeviceType.iPadAir;
                break;
            case "ipadair2":
            case "a1566":
            case "a1567":
                _TestDevice = DeviceType.iPadAir2;
                break;
            case "ios3":
            case "iphoneos3":
                _TestDevice = DeviceType.iOS3;
                break;
            case "ios4":
            case "iphoneos4":
                _TestDevice = DeviceType.iOS4;
                break;
            case "ios5":
            case "iphoneos5":
                _TestDevice = DeviceType.iOS5;
                break;
            case "ios6":
            case "iphoneos6":
                _TestDevice = DeviceType.iOS6;
                break;
            case "ios7":
            case "iphoneos7":
                _TestDevice = DeviceType.iOS7;
                break;
            case "ios9":
            case "iphoneos9":
                _TestDevice = DeviceType.iOS9;
                break;
            case "ios10":
            case "iphoneos10":
                _TestDevice = DeviceType.iOS10;
                break;
            case "ios11":
            case "iphoneos11":
                _TestDevice = DeviceType.iOS11;
                break;
            case "galaxy2":
            case "galaxys2":
            case "s2":
            case "sii":
            case "i9100":
            case "i9100g":
            case "i9105":
                _TestDevice = DeviceType.GalaxyS2;
                break;
            case "galaxy3":
            case "galaxys3":
            case "s3":
            case "siii":
            case "i9300":
            case "i9305":
                _TestDevice = DeviceType.GalaxyS3;
                break;
            case "galaxy4":
            case "galaxys4":
            case "s4":
            case "i9500":
            case "i9505":
                _TestDevice = DeviceType.GalaxyS4;
                break;
            case "galaxy5":
            case "galaxys5":
            case "s5":
            case "g900f":
            case "g800f":
            case "g900h":
            case "smg900":
            case "smg900f":
            case "smg800f":
            case "smg900h":
                _TestDevice = DeviceType.GalaxyS5;
                break;
            case "galaxy6":
            case "galaxys6":
            case "s6":
                _TestDevice = DeviceType.GalaxyS6;
                break;
            case "galaxy7":
            case "galaxys7":
            case "s7":
                _TestDevice = DeviceType.GalaxyS7;
                break;
            case "galaxy8":
            case "galaxys8":
            case "s8":
            case "smg950x":
                _TestDevice = DeviceType.GalaxyS8;
                break;
            case "galaxy9":
            case "galaxys9":
            case "s9":
            case "smg960x":
                _TestDevice = DeviceType.GalaxyS9;
                break;
            default:
                // throw new UnsupportedDevice(device);
                Logger.WriteLine(Logger.LogLevels.Error, String.format("Unsupported Device: [%s]", device));
                throw new RuntimeException(String.format("Unsupported Device: [%s]", device));
        }
    }
}
