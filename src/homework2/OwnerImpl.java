/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package homework2;

import homework2.bank.Account;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author David
 */
public class OwnerImpl extends UnicastRemoteObject implements Owner {
    
    private String name;
    private Account account;
    
    public OwnerImpl(String name, Account account) throws RemoteException{
        this.name=name;
        this.account=account;
    }
    
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
    
    @Override
    public Account getBankAccount() throws RemoteException {
        return account;
    }
    
}
