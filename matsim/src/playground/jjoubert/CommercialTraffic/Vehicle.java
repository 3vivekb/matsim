package playground.jjoubert.CommercialTraffic;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
/**
 * A vehicle class for the commercial vehicle study in South Africa. 
 * Each vehicle contains major activity locations, minor activity 
 * locations, and a list of all minor activities in the given study
 * area.
 *  
 * @author johanwjoubert
 *
 */
public class Vehicle {
	private int vehID;
	private ArrayList<Activity> homeLocation;
	private ArrayList<Chain> chains;
	//TODO Must generalize to cover ANY study area
	private ArrayList<Activity> studyAreaActivities;
	private int avgActivitesPerChain;
	private int avgChainDuration;
	private int avgChainDistance; 
	private int numberOfStudyAreaActivities;
	private float percentageStudyAreaActivities;
	private int studyAreaChainDistance;
	private int totalActivities;
	public final static int DISTANCE_THRESHOLD = 2500; // expressed in meters
	
	/**
	 * Creates a new vehicle with the ID as obtained from the 
	 * DigiCore data set.
	 * 
	 * @param id is the unique vehicle identification number used by DigiCore
	 */
	public Vehicle(int id){

		this.setVehID(id);
		this.homeLocation = new ArrayList<Activity>();
		this.chains = new ArrayList<Chain>();
		this.studyAreaActivities = new ArrayList<Activity>();
		this.avgActivitesPerChain = 0;
		this.avgChainDuration = 0;
		this.avgChainDistance = 0;
		this.numberOfStudyAreaActivities = 0;
		this.percentageStudyAreaActivities = 0;
		this.totalActivities = 0;
	}
	
	/**
	 * Updates a number of statistics for the vehicle:
	 * 		- calculates the average number of activities per chain;
	 * 		- calculates the average chain duration (in minutes);
	 * 		- calculates the average chain distance (in meters); and
	 * 		- calculates the average number of activities in the study
	 * 		  area.
	 * 
	 * Note: it is currently assumed that the study area is given in
	 *       the WGS84_UTM35S coordinate system.
	 *        
	 * @param studyArea of type <code>MultiPolygon<code> 
	 */
	public void updateVehicleStatistics(MultiPolygon studyArea){
		setAvgActivitiesPerChain();
		setAvgChainDuration();
		setAvgChainDistance();
		setNumberOfStudyAreaActivities( studyArea );
	}
	
	private void setAvgActivitiesPerChain(){
		int totalActivities = 0;
		if(this.chains.size() > 0){
			for (Chain chain : this.chains) {
				totalActivities += (chain.getActivities().size() - 2);
			}
			this.avgActivitesPerChain = ((int) ( totalActivities / this.chains.size() ));
		} else{
			this.avgActivitesPerChain = (0);
		}
		setTotalActivities(totalActivities);
	}

	private void setAvgChainDuration(){
		if(this.chains.size() > 0){
			int duration = 0;
			for (Chain chain : this.chains){
				duration += chain.getDuration();
			}
			this.avgChainDuration = ((int) (duration / this.chains.size() ));
		} else{
			this.avgChainDuration = (0);
		}
	}

	private void setAvgChainDistance() {
		if(this.chains.size() > 0){
			int totalDistance = 0;
			for(Chain chain: this.chains ){
				totalDistance += chain.getDistance();
			}
			this.avgChainDistance = ((totalDistance / this.chains.size() ));
		} else{
			this.avgChainDistance = (0);
		}
	}

	private void setNumberOfStudyAreaActivities(MultiPolygon studyArea){
		GeometryFactory gf = new GeometryFactory();
		if(this.chains.size() > 0){
			for (Chain chain : this.chains) {
				if(chain.getActivities().size() > 0){
					for (int i = 1; i < chain.getActivities().size() - 1; i++ ) { // don't count first and last major locations
						Activity thisActivity = chain.getActivities().get(i);
						Point p = gf.createPoint( thisActivity.getLocation().getCoordinate() );
						if( studyArea.contains(p) ){
							this.studyAreaActivities.add( thisActivity );
							chain.setInStudyArea(true);
						}
					}
				}
				if( chain.isInStudyArea() ){
					this.studyAreaChainDistance += chain.getDistance();
				}
			}
		}
		this.numberOfStudyAreaActivities = this.studyAreaActivities.size();
		this.percentageStudyAreaActivities = (((float) this.numberOfStudyAreaActivities) / 
										((float) this.getTotalActivities() ) );
	}
		
	/**
	 * Identify the major activity locations. The first chain's first
	 * major location is identified. All other major locations are 
	 * then checked, and only if it is further than a predefined 
	 * threshold of 2500m, will it be considered a 'new' location. 
	 * 
	 * The method adjusts all major location coordinates to the 
	 * weighted center of the location clusters. A second check is
	 * then done to see whether cluster centroids are within the 
	 * distance threshold. If so, the dominating cluster is the one
	 * with the most activity locations, and all dominated clusters'
	 * locations are adjusted accordingly.
	 */
	public void extractMajorLocations(){
		if(this.chains.size() > 0){
			// First find all possible major locations
			ArrayList<ArrayList<Activity>> majorLocationList = new ArrayList<ArrayList<Activity>>();
			ArrayList<Activity> newMajorLocation = new ArrayList<Activity>();
			majorLocationList.add(newMajorLocation);
			newMajorLocation.add( this.chains.get(0).getActivities().get(0) ); // add first home activity of first chain
			
			for(int i = 0; i < this.chains.size(); i++ ){
				Activity a1 = this.chains.get(i).getActivities().get( 
						this.chains.get(i).getActivities().size() - 1 ); // get the last activity of the chain
				Coordinate c1 = a1.getLocation().getCoordinate(); 
				
				// now check it against each potential major location
				boolean duplicate = false;
				int j = 0;
				while( (!duplicate) && (j < majorLocationList.size() ) ){
					ArrayList<Activity> thisMajorLocation = majorLocationList.get(j);
					int k = 0;
					while( (!duplicate) && (k < thisMajorLocation.size() ) ){
						try{
							Activity a2 = thisMajorLocation.get(k);
							Coordinate c2 = a2.getLocation().getCoordinate();
							int distance = (int)( c1.distance(c2) );
							if( distance < DISTANCE_THRESHOLD ){
								duplicate = true;
								majorLocationList.get(j).add(a1);
							} else{
								k++;
							}
						} catch(Exception e){
							System.out.print("");
						}
					}
					j++;
				}
				if( !duplicate ){ // then create a new major location
					newMajorLocation = new ArrayList<Activity>();
					newMajorLocation.add(a1);
					majorLocationList.add( newMajorLocation );
				}
			}
			
			// Now find the centre for each major location, and set the points for all locations the same.
			for(int i = 0; i < majorLocationList.size(); i++ ){
				ArrayList<Activity> thisList = majorLocationList.get(i);
				
				// Either the centroid of the polygon
//				ArrayList<Coordinate> coordList = new ArrayList<Coordinate>();
//				for(GPSPoint gps: thisList ){
//					coordList.add(gps.getCoordinate() );
//				}
//				Coordinate c[] = new Coordinate[coordList.size() + 1];
//				for(int j = 0; j < coordList.size(); j++ ){
//					c[j] = coordList.get(j);
//				}
//				c[coordList.size()] = coordList.get(0);
//				GeometryFactory gf = new GeometryFactory();
//				LinearRing lr = gf.createLinearRing(c);
//				Polygon p = gf.createPolygon(lr, null);
//				Coordinate center = p.getCentroid().getCoordinate();
				
				// Or just calculate the weighted average (gravity method...)
				double xSum = 0;
				double ySum = 0;
				for(int j = 0; j < thisList.size(); j++ ){
					GPSPoint point = thisList.get(j).getLocation();
					xSum += point.getCoordinate().x;
					ySum += point.getCoordinate().y;
				}
				Coordinate center = new Coordinate( (xSum / thisList.size()), (ySum / thisList.size()) );
				
				for(int j = 0; j < thisList.size(); j++ ){
					thisList.get(j).getLocation().setCoordinate(center);
				}				
			}
			
			// Consider one last pass if your first points turned out to be outliers.
			if(majorLocationList.size() > 1){
				int i = 0; 
				while( i < majorLocationList.size() - 1 ){
					boolean deleted = false;
					Coordinate c1 = majorLocationList.get(i).get(0).getLocation().getCoordinate();
					int w1 = majorLocationList.get(i).size();
					int j = (i+1); 
					while( j < majorLocationList.size() && !deleted ){
						Coordinate c2 = majorLocationList.get(j).get(0).getLocation().getCoordinate();
						int w2 = majorLocationList.get(j).size();
						int dist = (int) c1.distance(c2);
						if( dist < DISTANCE_THRESHOLD ){
							if( w1 > w2 ){ // move to point 1
								for(int k = 0; k < majorLocationList.get(j).size(); k++ ){
									majorLocationList.get(j).get(k).getLocation().setCoordinate(c1);
								}
								majorLocationList.get(i).addAll( majorLocationList.get(j) );
								majorLocationList.remove(j);
							} else {
								for(int k = 0; k < majorLocationList.get(i).size(); k++ ){
									majorLocationList.get(i).get(k).getLocation().setCoordinate(c2);
								}
								majorLocationList.get(j).addAll( majorLocationList.get(i) );
								majorLocationList.remove(i);
								deleted = true;
							} 
						} else{
							j++;
						}
					}
					if( !deleted ){
						i++;
					}
				}					
			}
			
			for( int i = 0; i < majorLocationList.size(); i++ ){
				homeLocation.add( majorLocationList.get(i).get(0) );
			}
			
		}
	}

	/**
	 * Considers each chain of the vehicle. If a chain does not start
	 * AND end at a major location, or if a chain contain at least one 
	 * minor activity between two major activities, the chain is 
	 * removed.
	 */
	public void cleanChains() {
		int i = 0;
		while (i < this.getChains().size() ){
			Activity first = this.getChains().get(i).getActivities().get(0);
			Activity last = this.getChains().get(i).getActivities().get(this.getChains().get(i).getActivities().size() - 1);
			if( (first.getDuration() < ActivityLocations.HOME_DURATION_THRESHOLD) ||
			    (last.getDuration() < ActivityLocations.HOME_DURATION_THRESHOLD) ||
			    (this.getChains().get(i).getActivities().size() <= 2 ) ){
				this.getChains().remove(i);
			} else{
				this.getChains().get(i).setDuration();
				this.getChains().get(i).setDistance();
				i++;
			}
		}
	}

	/**
	 * Returns the list of major activities. 
	 * 
	 * @return ArrayList < Activity > where <code>Activity<code> represents major activities.
	 */
	public ArrayList<Activity> getHomeLocation() {
		return homeLocation;
	}

	/**
	 * Returns the list of chains.
	 * 
	 * @return ArrayList < Chain >
	 */
	public ArrayList<Chain> getChains() {
		return chains;
	}

	/**
	 * Returns the list of all activities in Gauteng.
	 * 
	 * @return {@code ArrayList<Activity>}
	 */
	public ArrayList<Activity> getStudyAreaActivities() {
		return studyAreaActivities;
	}
	
	public void addChain(Chain chain){
		this.chains.add(chain);
	}

	public void setTotalActivities(int totalActivities) {
		this.totalActivities = totalActivities;
	}

	public int getTotalActivities() {
		return totalActivities;
	}

	private void setVehID(int vehID) {
		this.vehID = vehID;
	}

	public int getVehID() {
		return vehID;
	}

	public int getAvgActivitesPerChain() {
		return avgActivitesPerChain;
	}

	public int getAvgChainDuration() {
		return avgChainDuration;
	}

	public int getAvgChainDistance() {
		return avgChainDistance;
	}

	public float getPercentStudyAreaActivities() {
		return percentageStudyAreaActivities;
	}
		
	public int getNumberOfStudyAreaActivities() {
		return numberOfStudyAreaActivities;
	}

	public int getStudyAreaChainDistance() {
		return studyAreaChainDistance;
	}


	
}
