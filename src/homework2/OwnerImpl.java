/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package homework2;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author David
 */
public class OwnerImpl extends UnicastRemoteObject implements Owner {
    
    public String name;
    
    public OwnerImpl() throws RemoteException{}
    
    @Override
    public void itemSold(Item item) throws RemoteException {
        System.out.println("Your item ("+item.toString()+") has been sold!");
    }
    
    @Override
    public void wishAvaible(Item item) throws RemoteException {
        System.out.println("Your wish-item ("+item.toString()+") is now avaible!");
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }
    
}
