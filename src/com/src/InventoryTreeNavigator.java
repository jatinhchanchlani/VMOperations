package com.src;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.LocalizableMessage;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class InventoryTreeNavigator {

	private static void navigate(ServiceInstance si, Folder rootFolder, String vmFolderPath) throws InvalidProperty, RuntimeFault, RemoteException, InterruptedException
	{
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("Datacenter");
		
		String[] path = vmFolderPath.split("/");
		int count = path.length;
		int index =0;
		
			for(int i =0;i<mes.length;i++)
			{
				Datacenter dataCenter = (Datacenter) mes[i];
				Folder vmFolder = dataCenter.getVmFolder();
				if(count>0)
				{
					// Encountered the vmFolder so decrement the count by 1
					count--;
					index++;
					while(count>0) 
					{
						//Now till the count reaches zero, keep going in the child entity and find if there is a matching path
						//e.g there might be more than 1 child entity, so matching the name becomes important..
						//Doing it with the help of index
						ManagedEntity[] allVms = vmFolder.getChildEntity();
						for(ManagedEntity entity: allVms)
						{
							Folder folder = (Folder)entity;
							if(folder.getName().equals(path[index]))
							{
								vmFolder = folder;
								index++;
								count--;
								break;
							}
						}
					}
					// I have reached the correct folder.. Perform all the tasks
					getHostInformation(rootFolder);
					for(ManagedEntity entity: vmFolder.getChildEntity())
					{
						VirtualMachine vm = (VirtualMachine)entity;
						getVMInformation(si, rootFolder, vm.getName());
					}
				}
				else
				{
					// count is zero, so get all the vms in that datacenter
					ManagedEntity[] allVms = vmFolder.getChildEntity();
					getHostInformation(rootFolder);
					for(ManagedEntity entity: allVms)
					{
						VirtualMachine vm = (VirtualMachine)entity;
						
						getVMInformation(si, rootFolder, vm.getName());
					}
				}
				
			}
		
	}
	private static void performSnapshot(VirtualMachine vm)
	{
	
		try 
		{
			System.out.println("Performing Snapshot...");
			Task t = vm.createSnapshot_Task("VM2_Snapshot1", "HW2_Snapshot", false, true);
			String result = t.waitForTask();
			
			if(result.equalsIgnoreCase("success"))
			{
				System.out.println("Snapshot Successful");
			}
			else 
			{
				System.out.println("Error creating a Snapshot");
			}
			printTasks(vm);
			
		} 
		catch (RemoteException e) {
			
			System.out.println("Exception creating a snapshot");
		} catch (InterruptedException e) {
		
			System.out.println("Exception creating a snapshot");
		}
		
		
	}
	private static void clone(VirtualMachine vm)
	{
		  VirtualMachineCloneSpec spec = new VirtualMachineCloneSpec();
          VirtualMachineRelocateSpec vmrs = new VirtualMachineRelocateSpec();
          
          spec.setPowerOn(false);
          spec.setTemplate(false);
          spec.setLocation(vmrs);

          try {
        	  System.out.println("Performing Cloning...");
                Folder parent = (Folder) vm.getParent();
                Task task = vm.cloneVM_Task(parent, vm.getName()+"-clone", spec);

                task.waitForTask();
                if (task.getTaskInfo().getState() == TaskInfoState.error) {
                      System.out.println("Error cloning Virtual Machine");
                      System.out.println("Reason: " + task.getTaskInfo().getError().localizedMessage);
                }
                if (task.getTaskInfo().getState() == TaskInfoState.success) {
                      System.out.println("Cloning  successful.");
                      System.out.println("---------------------------------------------------------------");
                      Thread.sleep(1000);
                }
                printTasks(vm);
          } catch (Exception e) {
                System.out.println("Exception while cloning: " + e);
          }
	}
	private static void printTasks(VirtualMachine vm) throws InvalidProperty, RuntimeFault, RemoteException {
		
		System.out.println("Getting Recent Tasks for VM: " + vm.getName());	
		System.out.println("---------------------------------------------------------------");
		Task[] tasks = vm.getRecentTasks();
		for(Task t : tasks)
		{
			System.out.println("---------------------------------------------------------------");
			System.out.println("\tTask Name:  " + t.getTaskInfo().getName());
			System.out.println("\tTask Result:  " + t.getTaskInfo().getState());
			
			if(t.getTaskInfo().getState()==TaskInfoState.error)
			{
				LocalizableMessage msg = t.getTaskInfo().getDescription();
				String message ="";
				if (msg==null)
					message = "No Message";
				else
					message = msg.getMessage();
				System.out.println("\t\t Error Message: " + message);
				System.out.println("\t\t Error Reason: " + t.getTaskInfo().getError().localizedMessage);
			}
			System.out.println("\tTarget:  " + t.getTaskInfo().getEntityName());
			System.out.println("\tStart Time: "+t.getTaskInfo().getStartTime().getTime());
			System.out.println("\tComplete Time:  " + t.getTaskInfo().getCompleteTime().getTime());
		}
		System.out.println("---------------------------------------------------------------");
		
	}
	
	private static void migrate(ServiceInstance si, Folder rootFolder,VirtualMachine vm) throws InvalidProperty, RuntimeFault, RemoteException, InterruptedException
	{
		//get hosts
		boolean machineMigrated = false;
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
		if(mes.length>1)
		{
			// hosts>1
			HostSystem destination =null;
			for(int i =0; i<mes.length;i++)
			{
				HostSystem ds = (HostSystem)mes[i];
				VirtualMachine[] vms = ds.getVms();
				for(VirtualMachine tempVm: vms)
				{
					if(tempVm.getName().equals(vm.getName()))
					{
						System.out.println("vm found on host "+ ds.getName()+" .. ready to migrate to another host");

						//check compatibility of other destinations
						destination = selectDestination(si,rootFolder,vm,ds);
						System.out.println("Migrating VM on host: " + destination.getName());
						
						Task task = vm.migrateVM_Task(null, destination,VirtualMachineMovePriority.highPriority, 
							       vm.getRuntime().getPowerState());
						task.waitForTask();
						if (task.getTaskInfo().getState() == TaskInfoState.error) {
		                      System.out.println("Error migrating a Virtual Machine");
		                }
		                if (task.getTaskInfo().getState() == TaskInfoState.success) {
		                      System.out.println("Migration  successful.");
		                      System.out.println("---------------------------------------------------------------");
		                      Thread.sleep(1000);
		                }
		                printTasks(vm);
						
						return;		       
								 
					}
				}
				if(machineMigrated)
				{
					return;
				}
				
			}
		}
		else
		{
			System.out.println("Skipping Migration");
			System.out.println("Reason: Only 1 host found");
		}
		
	
	}

		public static void getHostInformation(Folder rootFolder) throws InvalidProperty, RuntimeFault, RemoteException
	{
		int hostCount = 0;
			
			ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
			
			for(ManagedEntity temp: mes)
			{
				HostSystem ds = (HostSystem)temp;
				System.out.println("---------------------Host Information-------------------------------");
				System.out.println("host[" + hostCount++ +"]");
				System.out.println("\tHost Name: "+ds.getName());
				System.out.println("\tProduct Full Name: " + ds.getConfig().getProduct().getFullName());
				System.out.println("---------------------------------------------------------------");
			}
		
		
	}
	public static void getVMInformation(ServiceInstance si, Folder rootFolder, String vmName) throws InvalidProperty, RuntimeFault, RemoteException, InterruptedException
	{
		boolean vmFound = false;
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		
		System.out.println("Getting the Virtual Machine Information..");
		for(int i =0; i< mes.length;i++)
		{
			
			VirtualMachine vm = (VirtualMachine)mes[i];
			if(vm.getName().equalsIgnoreCase(vmName))
			{
				System.out.println("---------------------------------------------------------------");
				System.out.println("\tVirtual Machine Name:  "+vm.getName());
				System.out.println("\tVM Guest OS Full Name: "+ vm.getConfig().getGuestFullName());
				System.out.println("\tVM Guest State:        "+ vm.getGuest().getGuestState());
				System.out.println("\tVM Power State:        "+vm.getRuntime().getPowerState());
				System.out.println("\tESXi Host:             "+vm.getSummary().getRuntime().getHost().get_value());
				System.out.println("---------------------------------------------------------------\n");
				vmFound = true;
				
				Thread.sleep(1000);
				performSnapshot(vm);
				Thread.sleep(1000);
				clone(vm);
				Thread.sleep(1000);
				migrate(si,rootFolder, vm);
				Thread.sleep(1000);
				printTasks(vm);
				break;
			}
					
		}
		
		if(!vmFound)
		{
			System.out.println("Virtual Machine with the name: " + vmName + " not found");
		}
	}
	
	private static HostSystem selectDestination(ServiceInstance si,Folder rootFolder, VirtualMachine vm, HostSystem ds) throws InvalidProperty, RuntimeFault, RemoteException 
	{
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
		for(int i =0; i<mes.length;)
		{
			HostSystem host = (HostSystem)mes[i];
			if(host.getName().equals(ds.getName()))
			{
				i++;
			}
			else
			{
				return host;
			}
			
			
		}
		return null;
		
	}
	
	public static void main(String[] args) {

		String ip = args[0];
		String login = args[1];
		String password = args[2];
		String vmFolder = args[3];
		 


		/*String ip = "https://192.168.79.130/sdk";
		String login = "root";
		String password = "1234567";
		*/

	/*	String ip = "https://130.65.159.14/sdk";
		String login = "vsphere.local\\cmpe283_sec3_student";
		String password = "cmpe283@sec3";
		String vmFolder = "CMPE LABS/CMPE283 SEC3/workspace/Jatin-939";*/

		ServiceInstance si = null;

		try 
		{
			si = new ServiceInstance(new URL(ip),login,password,true);
			Folder rootFolder = si.getRootFolder();
			
			navigate(si,rootFolder,vmFolder);

		}
		catch (RemoteException | MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
