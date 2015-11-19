/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package homework2;

import homework2.bank.Account;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author David
 */
public interface MarketRequest extends Remote {
    
    public String UploadItem(Item item) throws RemoteException;
    public List<Item> ListItems(Item item) throws RemoteException;
    public String BuyItem(Item item, Owner owner) throws RemoteException;
    public String AddWish(Item item) throws RemoteException;
    public String Register(Owner owner) throws RemoteException;
    public String Unregister(Owner owner) throws RemoteException;
    public Owner GetUser(String name) throws RemoteException;
}
