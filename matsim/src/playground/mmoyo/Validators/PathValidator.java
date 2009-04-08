package playground.mmoyo.Validators;

import org.matsim.core.api.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

public class PathValidator {

	public PathValidator (){
	
	}
	/*Conditions:
	 * No null
	 * must have at least 3 links
	 * starts and ends with walking links
	 * transfer links must be always between standard links
	 * must not have two adjacent walking links
	 */
	public boolean isValid(Path path) {
		boolean valid = true;
		
		if (path!=null){
			//that path starts and ends with walking links
			if (path.links.size()>2 && path.links.get(0).getType().equals("Walking") && path.links.get(path.links.size()-1).getType().equals("Walking") ){
				boolean hasStandardLinks = false;
				String linkType;
				int i=0;
				
				for (Link link : path.links) {
					linkType= link.getType();
					if (linkType.equals("Standard")) {
						hasStandardLinks=true;
					}else if(linkType.equals("Transfer")){
						if (i>0){  
							if (path.links.get(i-1).getType()!="Standard" || path.links.get(i+1).getType()!="Standard"){ //TODO:check that transfer links are only between standard link
								return false;
							}
						}
					}else if (linkType=="Walking"){
						if (i>0){
							if (path.links.get(i-1).getType()=="Walking"){ //TODO:check that do not have two adjacent walking link
								return false;
							}
						}
					}else if(linkType =="DetTransfer"){
						if (i>0){  
							if (path.links.get(i-1).getType()!="Standard" || path.links.get(i+1).getType()!="Standard"){
								return false;
							}
						}
					}//linktype
					i++;
				}//for interator
				if(hasStandardLinks==false){return false;}
			}else{
				valid=false;
			}//if pathlinks
		}else{
			valid=false;
		}//path!=null
		return valid;
	}//is valid
	
	
	public void printPath(Path path){
		System.out.print(path.toString());
		/*
		for (Link link : path.links){
			System.out.print(b);
			//link.getId()
		}
		*/	
	}
	
	
}//class
