package com.judemanutd.autostarter

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.util.*

object AutoStartPermissionHelper {

    /***
     * Xiaomi
     */
    private const val BRAND_XIAOMI = "xiaomi"
    private const val BRAND_XIAOMI_POCO = "poco"
    private const val BRAND_XIAOMI_REDMI = "redmi"
    private const val PACKAGE_XIAOMI_MAIN = "com.miui.securitycenter"
    private const val PACKAGE_XIAOMI_COMPONENT =
            "com.miui.permcenter.autostart.AutoStartManagementActivity"

    /***
     * Letv
     */
    private const val BRAND_LETV = "letv"
    private const val PACKAGE_LETV_MAIN = "com.letv.android.letvsafe"
    private const val PACKAGE_LETV_COMPONENT = "com.letv.android.letvsafe.AutobootManageActivity"

    /***
     * ASUS ROG
     */
    private const val BRAND_ASUS = "asus"
    private const val PACKAGE_ASUS_MAIN = "com.asus.mobilemanager"
    private const val PACKAGE_ASUS_COMPONENT = "com.asus.mobilemanager.powersaver.PowerSaverSettings"
    private const val PACKAGE_ASUS_COMPONENT_FALLBACK =
            "com.asus.mobilemanager.autostart.AutoStartActivity"

    /***
     * Honor
     */
    private const val BRAND_HONOR = "honor"
    private const val PACKAGE_HONOR_MAIN = "com.huawei.systemmanager"
    private const val PACKAGE_HONOR_COMPONENT =
            "com.huawei.systemmanager.optimize.process.ProtectActivity"

    /***
     * Huawei
     */
    private const val BRAND_HUAWEI = "huawei"
    private const val PACKAGE_HUAWEI_MAIN = "com.huawei.systemmanager"
    private const val PACKAGE_HUAWEI_COMPONENT =
            "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
    private const val PACKAGE_HUAWEI_COMPONENT_FALLBACK =
            "com.huawei.systemmanager.optimize.process.ProtectActivity"

    /**
     * Oppo
     */
    private const val BRAND_OPPO = "oppo"
    private const val PACKAGE_OPPO_MAIN = "com.coloros.safecenter"
    private const val PACKAGE_OPPO_FALLBACK = "com.oppo.safe"
    private const val PACKAGE_OPPO_COMPONENT =
            "com.coloros.safecenter.permission.startup.StartupAppListActivity"
    private const val PACKAGE_OPPO_COMPONENT_FALLBACK =
            "com.oppo.safe.permission.startup.StartupAppListActivity"
    private const val PACKAGE_OPPO_COMPONENT_FALLBACK_A =
            "com.coloros.safecenter.startupapp.StartupAppListActivity"

    /**
     * Vivo
     */

    private const val BRAND_VIVO = "vivo"
    private const val PACKAGE_VIVO_MAIN = "com.iqoo.secure"
    private const val PACKAGE_VIVO_FALLBACK = "com.vivo.permissionmanager"
    private const val PACKAGE_VIVO_COMPONENT = "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
    private const val PACKAGE_VIVO_COMPONENT_FALLBACK =
            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
    private const val PACKAGE_VIVO_COMPONENT_FALLBACK_A =
            "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"

    /**
     * Nokia
     */

    private const val BRAND_NOKIA = "nokia"
    private const val PACKAGE_NOKIA_MAIN = "com.evenwell.powersaving.g3"
    private const val PACKAGE_NOKIA_COMPONENT =
            "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity"

    /***
     * Samsung
     */
    private const val BRAND_SAMSUNG = "samsung"
    private const val PACKAGE_SAMSUNG_MAIN = "com.samsung.android.lool"
    private const val PACKAGE_SAMSUNG_COMPONENT = "com.samsung.android.sm.ui.battery.BatteryActivity"
    private const val PACKAGE_SAMSUNG_COMPONENT_2 =
            "com.samsung.android.sm.battery.ui.usage.CheckableAppListActivity"
    private const val PACKAGE_SAMSUNG_COMPONENT_3 = "com.samsung.android.sm.battery.ui.BatteryActivity"

    /***
     * One plus
     */
    private const val BRAND_ONE_PLUS = "oneplus"
    private const val PACKAGE_ONE_PLUS_MAIN = "com.oneplus.security"
    private const val PACKAGE_ONE_PLUS_COMPONENT =
            "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"

    private val PACKAGES_TO_CHECK_FOR_PERMISSION = listOf(
            PACKAGE_ASUS_MAIN,
            PACKAGE_XIAOMI_MAIN,
            PACKAGE_LETV_MAIN,
            PACKAGE_HONOR_MAIN,
            PACKAGE_OPPO_MAIN,
            PACKAGE_OPPO_FALLBACK,
            PACKAGE_VIVO_MAIN,
            PACKAGE_VIVO_FALLBACK,
            PACKAGE_NOKIA_MAIN,
            PACKAGE_HUAWEI_MAIN,
            PACKAGE_SAMSUNG_MAIN,
            PACKAGE_ONE_PLUS_MAIN
    )

    private val brand = Build.BRAND.lowercase(Locale.ROOT)

    /**
     * It will attempt to open the specific manufacturer settings screen with the autostart permission
     *
     * @param context
     * @param newTask, if true when the activity is attempted to be opened it will add FLAG_ACTIVITY_NEW_TASK to the intent
     * @return true if the activity was opened
     */
    fun getAutoStartPermission(
            context: Context,
            newTask: Boolean = false
    ): Boolean {
        return getPackagesAndIntents(newTask)?.let { packagesAndIntents ->
            val activityFound = autoStart(context, packagesAndIntents.first, packagesAndIntents.second)
            if (!activityFound && brand == BRAND_OPPO) {
                launchOppoAppInfo(context, newTask)
            } else {
                activityFound
            }
        } ?: false
    }

    /**
     * Checks whether the autostart permission is present in the manufacturer and supported by the library
     *
     * @param context
     * @param onlyIfSupported if true, the method will only return true if the screen is supported by the library.
     *          If false, the method will return true as long as the permission exist even if the screen is not supported
     *          by the library.
     * @return true if autostart permission is present in the manufacturer and supported by the library, false otherwise
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun isAutoStartPermissionAvailable(
            context: Context,
            onlyIfSupported: Boolean = false
    ): Boolean {
        val packages: List<ApplicationInfo>
        val pm = context.packageManager
        packages = pm.getInstalledApplications(0)
        return packages.any { PACKAGES_TO_CHECK_FOR_PERMISSION.contains(it.packageName) } && (!onlyIfSupported || run {
            getPackagesAndIntents(false)?.second?.any { intent ->
                isActivityFound(context, intent)
            } ?: false
        })
    }

    /**
     * Checks which autostart permission intent is present in the manufacturer
     *
     * @param context
     * @return the intent that is resolvable to an activity, or null of none found
     */
    fun getResolvedAutoStartPermissionIntent(context: Context): Intent? {
        getPackagesAndIntents(false)?.second?.forEach { intent ->
            if (isActivityFound(context, intent)) {
                return intent
            }
        }

        return null
    }

    private fun getPackagesAndIntents(newTask: Boolean): Pair<List<String>, List<Intent>>? {
        return when (brand) {
            BRAND_ASUS -> getAsusPackagesAndIntents(newTask)
            BRAND_XIAOMI, BRAND_XIAOMI_POCO, BRAND_XIAOMI_REDMI -> getXiaomiPackagesAndIntents(newTask)
            BRAND_LETV -> getLetvPackagesAndIntents(newTask)
            BRAND_HONOR -> getHonorPackagesAndIntents(newTask)
            BRAND_HUAWEI -> getHuaweiPackagesAndIntents(newTask)
            BRAND_OPPO -> getOppoPackagesAndIntents(newTask)
            BRAND_VIVO -> getVivoPackagesAndIntents(newTask)
            BRAND_NOKIA -> getNokiaPackagesAndIntents(newTask)
            BRAND_SAMSUNG -> getSamsungPackagesAndIntents(newTask)
            BRAND_ONE_PLUS -> getOnePlusPackagesAndIntents(newTask)
            else -> null
        }
    }

    private fun getXiaomiPackagesAndIntents(newTask: Boolean): Pair<List<String>, List<Intent>> {
        return listOf(PACKAGE_XIAOMI_MAIN) to listOf(getIntent(PACKAGE_XIAOMI_MAIN, PACKAGE_XIAOMI_COMPONENT, newTask))
    }

    private fun getAsusPackagesAndIntents(newTask: Boolean): Pair<List<String>, List<Intent>> {
        return listOf(PACKAGE_ASUS_MAIN) to listOf(
                getIntent(PACKAGE_ASUS_MAIN, PACKAGE_ASUS_COMPONENT, newTask),
                getIntent(PACKAGE_ASUS_MAIN, PACKAGE_ASUS_COMPONENT_FALLBACK, newTask)
        )
    }

    private fun getLetvPackagesAndIntents(newTask: Boolean): Pair<List<String>, List<Intent>> {
        return listOf(PACKAGE_LETV_MAIN) to listOf(getIntent(PACKAGE_LETV_MAIN, PACKAGE_LETV_COMPONENT, newTask))
    }

    private fun getHonorPackagesAndIntents(newTask: Boolean): Pair<List<String>, List<Intent>> {
        return listOf(PACKAGE_HONOR_MAIN) to listOf(getIntent(PACKAGE_HONOR_MAIN, PACKAGE_HONOR_COMPONENT, newTask))
    }

    private fun getHuaweiPackagesAndIntents(newTask: Boolean): Pair<List<String>, List<Intent>> {
        return listOf(PACKAGE_HUAWEI_MAIN) to listOf(
                getIntent(PACKAGE_HUAWEI_MAIN, PACKAGE_HUAWEI_COMPONENT, newTask),
                getIntent(PACKAGE_HUAWEI_MAIN, PACKAGE_HUAWEI_COMPONENT_FALLBACK, newTask)
        )
    }

    private fun getOppoPackagesAndIntents(newTask: Boolean): Pair<List<String>, List<Intent>> {
        return listOf(PACKAGE_OPPO_MAIN, PACKAGE_OPPO_FALLBACK) to listOf(
                getIntent(PACKAGE_OPPO_MAIN, PACKAGE_OPPO_COMPONENT, newTask),
                getIntent(PACKAGE_OPPO_FALLBACK, PACKAGE_OPPO_COMPONENT_FALLBACK, newTask),
                getIntent(PACKAGE_OPPO_MAIN, PACKAGE_OPPO_COMPONENT_FALLBACK_A, newTask)
        )
    }

    private fun launchOppoAppInfo(context: Context, newTask: Boolean): Boolean {
        return try {
            val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            i.addCategory(Intent.CATEGORY_DEFAULT)
            i.data = Uri.parse("package:${context.packageName}")

            if (newTask) i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(i)
            true
        } catch (exx: Exception) {
            exx.printStackTrace()
            false
        }
    }

    private fun getVivoPackagesAndIntents(newTask: Boolean): Pair<List<String>, List<Intent>> {
        return listOf(PACKAGE_VIVO_MAIN, PACKAGE_VIVO_FALLBACK) to listOf(
                getIntent(PACKAGE_VIVO_MAIN, PACKAGE_VIVO_COMPONENT, newTask),
                getIntent(PACKAGE_VIVO_FALLBACK, PACKAGE_VIVO_COMPONENT_FALLBACK, newTask),
                getIntent(PACKAGE_VIVO_MAIN, PACKAGE_VIVO_COMPONENT_FALLBACK_A, newTask)
        )
    }

    private fun getNokiaPackagesAndIntents(newTask: Boolean): Pair<List<String>, List<Intent>> {
        return listOf(PACKAGE_NOKIA_MAIN) to listOf(getIntent(PACKAGE_NOKIA_MAIN, PACKAGE_NOKIA_COMPONENT, newTask))
    }

    private fun getSamsungPackagesAndIntents(newTask: Boolean): Pair<List<String>, List<Intent>> {
        return listOf(PACKAGE_SAMSUNG_MAIN) to listOf(
                getIntent(PACKAGE_SAMSUNG_MAIN, PACKAGE_SAMSUNG_COMPONENT, newTask),
                getIntent(PACKAGE_SAMSUNG_MAIN, PACKAGE_SAMSUNG_COMPONENT_2, newTask),
                getIntent(PACKAGE_SAMSUNG_MAIN, PACKAGE_SAMSUNG_COMPONENT_3, newTask)
        )
    }

    private fun getOnePlusPackagesAndIntents(newTask: Boolean): Pair<List<String>, List<Intent>> {
        return listOf(PACKAGE_ONE_PLUS_MAIN) to listOf(getIntent(PACKAGE_ONE_PLUS_MAIN, PACKAGE_ONE_PLUS_COMPONENT, newTask))
    }

    @Throws(Exception::class)
    private fun startIntent(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (exception: Exception) {
            exception.printStackTrace()
            throw exception
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun isPackageExists(context: Context, targetPackage: String): Boolean {
        val packages: List<ApplicationInfo>
        val pm = context.packageManager
        packages = pm.getInstalledApplications(0)
        for (packageInfo in packages) {
            if (packageInfo.packageName == targetPackage) {
                return true
            }
        }
        return false
    }

    /**
     * Generates an intent with the passed package and component name
     * @param packageName
     * @param componentName
     * @param newTask
     *
     * @return the intent generated
     */
    private fun getIntent(packageName: String, componentName: String, newTask: Boolean): Intent {
        return Intent().apply {
            component = ComponentName(packageName, componentName)
            if (newTask) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Will query the passed intent to check whether the Activity really exists
     *
     * @param context
     * @param intent, intent to open an activity
     *
     * @return true if activity is found, false otherwise
     */
    private fun isActivityFound(context: Context, intent: Intent): Boolean {
        return context.packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY
        ).isNotEmpty()
    }

    /**
     * Will attempt to open the AutoStart settings activity from the passed list of intents in order.
     * The first activity found will be opened.
     *
     * @param context
     * @param intents list of intents
     *
     * @return true if an activity was opened, false otherwise
     */
    private fun openAutoStartScreen(context: Context, intents: List<Intent>): Boolean {
        intents.forEach {
            if (isActivityFound(context, it)) {
                startIntent(context, it)
                return@openAutoStartScreen true
            }
        }
        return false
    }

    /**
     * Will trigger the common autostart permission logic. If [open] is true it will attempt to open the specific
     * manufacturer setting screen, otherwise it will just check for its existence
     *
     * @param context
     * @param packages, list of known packages of the corresponding manufacturer
     * @param intents, list of known intents that open the corresponding manufacturer settings screens
     * @param open, if true it will attempt to open the settings screen, otherwise it just check its existence
     * @return true if the screen was opened or exists, false if it doesn't exist or could not be opened
     */
    private fun autoStart(
            context: Context,
            packages: List<String>,
            intents: List<Intent>,
    ): Boolean {
        return if (packages.any { isPackageExists(context, it) }) {
            openAutoStartScreen(context, intents)
        } else false
    }

}
