package com.binbin.androidowner.dpm

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.*
import android.content.*
import android.os.Binder
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.binbin.androidowner.R
import com.binbin.androidowner.ui.*
import com.binbin.androidowner.ui.theme.bgColor

@Composable
fun ManagedProfile(navCtrl: NavHostController) {
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    /*val titleMap = mapOf(
        "OrgOwnedWorkProfile" to R.string.org_owned_work_profile,
        "CreateWorkProfile" to R.string.create_work_profile,
        "SuspendPersonalApp" to R.string.suspend_personal_app,
        "IntentFilter" to R.string.intent_filter,
        "OrgID" to R.string.org_id
    )*/
    Scaffold(
        topBar = {
            /*TopAppBar(
                title = {Text(text = stringResource(titleMap[backStackEntry?.destination?.route]?:R.string.work_profile))},
                navigationIcon = {NavIcon{if(backStackEntry?.destination?.route=="Home"){navCtrl.navigateUp()}else{localNavCtrl.navigateUp()}}},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surfaceVariant)
            )*/
            TopBar(backStackEntry,navCtrl, localNavCtrl)
        }
    ){
        NavHost(
            navController = localNavCtrl, startDestination = "Home",
            enterTransition = Animations().navHostEnterTransition,
            exitTransition = Animations().navHostExitTransition,
            popEnterTransition = Animations().navHostPopEnterTransition,
            popExitTransition = Animations().navHostPopExitTransition,
            modifier = Modifier.background(bgColor).padding(top = it.calculateTopPadding())
        ){
            composable(route = "Home"){Home(localNavCtrl)}
            composable(route = "OrgOwnedWorkProfile"){OrgOwnedProfile()}
            composable(route = "CreateWorkProfile"){CreateWorkProfile()}
            composable(route = "SuspendPersonalApp"){SuspendPersonalApp()}
            composable(route = "IntentFilter"){IntentFilter()}
            composable(route = "OrgID"){OrgID()}
        }
    }
}

@Composable
private fun Home(navCtrl: NavHostController){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())){
        Text(text = stringResource(R.string.work_profile), style = typography.headlineLarge, modifier = Modifier.padding(top = 8.dp, bottom = 5.dp, start = 15.dp))
        if(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)){
            SubPageItem(R.string.org_owned_work_profile,"",R.drawable.corporate_fare_fill0){navCtrl.navigate("OrgOwnedWorkProfile")}
        }
        if(VERSION.SDK_INT<24||(VERSION.SDK_INT>=24&&myDpm.isProvisioningAllowed(ACTION_PROVISION_MANAGED_PROFILE))){
            SubPageItem(R.string.create_work_profile,"",R.drawable.work_fill0){navCtrl.navigate("CreateWorkProfile")}
        }
        if(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile){
            SubPageItem(R.string.suspend_personal_app,"",R.drawable.block_fill0){navCtrl.navigate("SuspendPersonalApp")}
        }
        if(isProfileOwner(myDpm)&&(VERSION.SDK_INT<24||(VERSION.SDK_INT>=24&&myDpm.isManagedProfile(myComponent)))){
            SubPageItem(R.string.intent_filter,"",R.drawable.filter_alt_fill0){navCtrl.navigate("IntentFilter")}
        }
        if(VERSION.SDK_INT>=31&&(isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent))){
            SubPageItem(R.string.org_id,"",R.drawable.corporate_fare_fill0){navCtrl.navigate("OrgID")}
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun CreateWorkProfile(){
    val myContext = LocalContext.current
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.create_work_profile), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        var skipEncrypt by remember{mutableStateOf(false)}
        if(VERSION.SDK_INT>=24){CheckBoxItem(stringResource(R.string.skip_encryption),{skipEncrypt},{skipEncrypt=!skipEncrypt})}
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                try {
                    val intent = Intent(ACTION_PROVISION_MANAGED_PROFILE)
                    if(VERSION.SDK_INT>=23){
                        intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,myComponent)
                    }else{
                        intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME, myContext.packageName)
                    }
                    if(VERSION.SDK_INT>=24){intent.putExtra(EXTRA_PROVISIONING_SKIP_ENCRYPTION,skipEncrypt)}
                    if(VERSION.SDK_INT>=33){intent.putExtra(EXTRA_PROVISIONING_ALLOW_OFFLINE,true)}
                    createManagedProfile.launch(intent)
                }catch(e:ActivityNotFoundException){
                    Toast.makeText(myContext,myContext.getString(R.string.unsupported),Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.create))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun OrgOwnedProfile(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.org_owned_work_profile), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.is_org_owned_profile,myDpm.isOrganizationOwnedDeviceWithManagedProfile))
        Spacer(Modifier.padding(vertical = 5.dp))
        if(!myDpm.isOrganizationOwnedDeviceWithManagedProfile){
            SelectionContainer {
                Text(
                    text = stringResource(R.string.activate_org_profile_command, Binder.getCallingUid()/100000),
                    color = colorScheme.onTertiaryContainer
                )
            }
            CopyTextButton(myContext, R.string.copy_code, stringResource(R.string.activate_org_profile_command, Binder.getCallingUid()/100000))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun OrgID(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        var orgId by remember{mutableStateOf("")}
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.org_id), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = orgId, onValueChange = {orgId=it},
            label = {Text(stringResource(R.string.org_id))},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
            modifier = Modifier.focusable().fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 2.dp))
        AnimatedVisibility(orgId.length !in 6..64) {
            Text(text = stringResource(R.string.length_6_to_64))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                myDpm.setOrganizationId(orgId)
                Toast.makeText(myContext, myContext.getString(R.string.success),Toast.LENGTH_SHORT).show()
            },
            enabled = orgId.length in 6..64,
            modifier = Modifier.fillMaxWidth()
        ){
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Information{Text(text = stringResource(R.string.get_specific_id_after_set_org_id))}
    }
}

@SuppressLint("NewApi")
@Composable
private fun SuspendPersonalApp(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        SwitchItem(
            R.string.suspend_personal_app,"",null,{myDpm.getPersonalAppsSuspendedReasons(myComponent)!=PERSONAL_APPS_NOT_SUSPENDED},
            {myDpm.setPersonalAppsSuspended(myComponent,it)}
        )
        var time by remember{mutableStateOf("")}
        time = myDpm.getManagedProfileMaximumTimeOff(myComponent).toString()
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.profile_max_time_off), style = typography.titleLarge)
        Text(text = stringResource(R.string.profile_max_time_out_desc))
        Text(text = stringResource(R.string.personal_app_suspended_because_timeout, myDpm.getPersonalAppsSuspendedReasons(myComponent)==PERSONAL_APPS_SUSPENDED_PROFILE_TIMEOUT))
        OutlinedTextField(
            value = time, onValueChange = {time=it}, modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp),
            label = {Text(stringResource(R.string.time_unit_ms))},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
        )
        Text(text = stringResource(R.string.cannot_less_than_72_hours))
        Button(
            onClick = {
                myDpm.setManagedProfileMaximumTimeOff(myComponent,time.toLong())
                Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@Composable
private fun IntentFilter(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        var action by remember{mutableStateOf("")}
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.intent_filter), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = action, onValueChange = {action = it},
            label = {Text("Action")},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
            modifier = Modifier.focusable().fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                myDpm.addCrossProfileIntentFilter(myComponent, IntentFilter(action), FLAG_PARENT_CAN_ACCESS_MANAGED)
                Toast.makeText(myContext, myContext.getString(R.string.success),Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_intent_filter_work_to_personal))
        }
        Button(
            onClick = {
                myDpm.addCrossProfileIntentFilter(myComponent, IntentFilter(action), FLAG_MANAGED_CAN_ACCESS_PARENT)
                Toast.makeText(myContext, myContext.getString(R.string.success),Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_intent_filter_personal_to_work))
        }
        Spacer(Modifier.padding(vertical = 2.dp))
        Button(
            onClick = {
                myDpm.clearCrossProfileIntentFilters(myComponent)
                Toast.makeText(myContext, myContext.getString(R.string.success),Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text(stringResource(R.string.clear_cross_profile_filters))
        }
    }
}
