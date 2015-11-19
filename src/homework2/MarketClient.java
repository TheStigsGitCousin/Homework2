/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package homework2;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David
 */
public class MarketClient {
    
    public MarketClient(String clientName) throws RemoteException{
//        Owner ownerObject=new OwnerImpl();
        // Register the newly created object at rmiregistry.
        try {
            LocateRegistry.getRegistry(1099).list();
        } catch (RemoteException e) {
            LocateRegistry.createRegistry(1099);
        }
//        try {
//            Naming.rebind(clientName, ownerObject);
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(MarketClient.class.getName()).log(Level.SEVERE, null, ex);
//        }
        Map<String, List<String>> map=new HashMap<>();
        List<String> list=new ArrayList<>();
        list.add("first item");
        map.put("hello",list);
        List<String> list2=map.get("hello");
        list2.add("Second item");
        for(String s : map.get("hello")){
            System.out.println(s);
        }
    }
    
    public static void main(String[] args) throws RemoteException {
        new MarketClient("clientName");
    }
}
