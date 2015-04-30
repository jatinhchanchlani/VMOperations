package com.src;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VMOperations {

	/**
	 * This metho will get the VM information
	 * @param rootFolder
	 * @throws InvalidProperty
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	public static void getVMInformation(Folder rootFolder) throws InvalidProperty, RuntimeFault, RemoteException
	{
		
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		
		System.out.println("Getting all the Virtual Machines Information..");
		for(int i =0; i< mes.length;i++)
		{
			System.out.println("---------------------------------------------------------------");
			System.out.println("virtualMachine[" + i+ "]");
			VirtualMachine vm = (VirtualMachine)mes[i];
			System.out.println("\tVirtual Machine Name:  "+vm.getName());
			System.out.println("\tVM Guest OS Full Name: "+ vm.getConfig().getGuestFullName());
			System.out.println("\tVM Guest State:        "+ vm.getGuest().getGuestState());
			System.out.println("\tVMware Tools:          "+vm.getGuest().getToolsRunningStatus());
			System.out.println("\tVMware Power State:    "+vm.getRuntime().getPowerState()); 
			System.out.println("---------------------------------------------------------------\n");
			
		}
	}
	/**
	 * This method will power off all the VMs
	 * @param rootFolder
	 * @throws InvalidProperty
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	public static void powerOffVMS(Folder rootFolder) throws InvalidProperty, RuntimeFault, RemoteException
	{
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		System.out.println("Powering Off both the virtual machines");
		for(int i =0; i< mes.length;i++)
		{
			
			VirtualMachine vm = (VirtualMachine)mes[i];
			
			vm.powerOffVM_Task();
			
		}
	}
	/**
	 * This method will power on all the VMs
	 * @param rootFolder
	 * @throws InvalidProperty
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	public static void powerOnVMS(Folder rootFolder) throws InvalidProperty, RuntimeFault, RemoteException
	{
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		ManagedEntity[] mes1 = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
	
		System.out.println("Powering On both the virtual machines");
		for(int j =0;j<mes1.length;j++)
		{
			HostSystem ds = (HostSystem)mes1[j];
			for(int i =0; i< mes.length;i++)
			{
				
				VirtualMachine vm = (VirtualMachine)mes[i];
				vm.powerOnVM_Task(ds);
				
			}
		}	
		System.out.println("done");
	}
	/**
	 * This method will print the recent tasks of all the VMs
	 * @param rootFolder
	 * @throws InvalidProperty
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	public static void recentTasks(Folder rootFolder) throws InvalidProperty, RuntimeFault, RemoteException
	{
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		
		for(int i =0; i< mes.length;i++)
		{
			
			VirtualMachine vm = (VirtualMachine)mes[i];
			
			System.out.println("Getting Recent Tasks for VM: " + vm.getName());	
			System.out.println("---------------------------------------------------------------");
			Task[] tasks = vm.getRecentTasks();
			for(Task t : tasks)
			{
				
				System.out.println("\tTask Name:  " + t.getTaskInfo().getName());
				System.out.println("\tStart Time: "+t.getTaskInfo().getStartTime().getTime());
			}
			System.out.println("---------------------------------------------------------------");
		}
	}
	/**
	 * This method will power on the 1st VM and power off the 2nd VM
	 * @param rootFolder
	 * @throws InvalidProperty
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	public static void powerOff1_powerOn2(Folder rootFolder) throws InvalidProperty, RuntimeFault, RemoteException
	{
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		ManagedEntity[] mes1 = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
		HostSystem host = (HostSystem)mes1[0];
		for(int i =0; i< mes.length;i++)
		{
			
			VirtualMachine vm = (VirtualMachine)mes[i];
			if(vm.getName().equals("ubuntu1404-939-1"))
			{
				System.out.println("VM Name: " + vm.getName());
				System.out.println("VM Current State:" + vm.getRuntime().getPowerState());
				System.out.println("Powering On " +  vm.getName());
				vm.powerOnVM_Task(host);
			}
			else if(vm.getName().equals("ubuntu1404-939-2"))
			{
				System.out.println("VM Name: " + vm.getName());
				System.out.println("VM Current State:" + vm.getRuntime().getPowerState());
				System.out.println("Powering Off " +  vm.getName());
				vm.powerOffVM_Task();
			}
		}
	}
	/**
	 * This method will power off the 1st VM and power on the 2nd VM
	 * @param rootFolder
	 * @throws InvalidProperty
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	public static void powerOff2_powerOn1(Folder rootFolder) throws InvalidProperty, RuntimeFault, RemoteException
	{
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		ManagedEntity[] mes1 = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
		HostSystem host = (HostSystem)mes1[0];
		for(int i =0; i< mes.length;i++)
		{
			
			VirtualMachine vm = (VirtualMachine)mes[i];
			if(vm.getName().equals("ubuntu1404-939-1"))
			{
				System.out.println("VM Name: " + vm.getName());
				System.out.println("VM Current State:" + vm.getRuntime().getPowerState());
				System.out.println("Powering Off " +  vm.getName());
				vm.powerOffVM_Task();
			}
			else if(vm.getName().equals("ubuntu1404-939-2"))
			{
				System.out.println("VM Name: " + vm.getName());
				System.out.println("VM Current State:" + vm.getRuntime().getPowerState());
				System.out.println("Powering On " +  vm.getName());
				vm.powerOnVM_Task(host);
			}
		}
	}
	/**
	 * This method is responsible will iterate over the host and print out its information along with its datastore and network information
	 * @param rootFolder 
	 * @throws InvalidProperty
	 * @throws RuntimeFault
	 * @throws RemoteException
	 */
	public static void getHostInformation(Folder rootFolder) throws InvalidProperty, RuntimeFault, RemoteException
	{
		int hostCount = 0,dataStoreCount = 0, networkCount = 0;
			
			ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
			
			for(ManagedEntity temp: mes)
			{
				HostSystem ds = (HostSystem)temp;
				System.out.println("---------------------------------------------------------------");
				System.out.println("host[" + hostCount++ +"]");
				System.out.println("\tHost Name: "+ds.getName());
				System.out.println("\tProduct Full Name: " + ds.getConfig().getProduct().getFullName());
				
				Datastore[] dataStores = ds.getDatastores();
				
				for(Datastore tempDataStore: dataStores)
				{
					System.out.println("\n\tdatastore[" + dataStoreCount++ +"]");
					System.out.println("\t\tDataStore Name: "+tempDataStore.getName());
					System.out.println("\t\tDataStore Available Space: "+tempDataStore.getSummary().freeSpace);
					System.out.println("\t\tDataStore Capacity: "+tempDataStore.getSummary().capacity);
				}
				
				Network[] networks = ds.getNetworks();
				for(Network tempNw : networks)
				{
					System.out.println("\n\tnetwork[" + networkCount++ +"]");
					System.out.println("\t\tNetwork Name: "+tempNw.getSummary().getName());
				}
				
				System.out.println("---------------------------------------------------------------");
			}
		
		
	}
	public static void main(String[] arg) {
		
		ServiceInstance si = null;
		
		try
		{
			
			/*  arg[0] - url https://192.168.79.130/sdk
				arg[1] - username root
				arg[2] - password 1234567
			*/
			si = new ServiceInstance(new URL(arg[0]),arg[1],arg[2],true);
			Folder rootFolder = si.getRootFolder();
			
			getHostInformation(rootFolder);
			getVMInformation(rootFolder);
			powerOffVMS(rootFolder);
			powerOnVMS(rootFolder);
			recentTasks(rootFolder);
			powerOff1_powerOn2(rootFolder);
			powerOff2_powerOn1(rootFolder);
		}
		catch (RemoteException e) 
		{
			e.printStackTrace();
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			si.getServerConnection().logout();
		}
		
		
	}
}
