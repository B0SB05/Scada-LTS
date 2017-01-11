package org.scada_lts.web.mvc.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scada_lts.mango.service.DataPointService;
import org.scada_lts.mango.service.PointValueService;
import org.scada_lts.mango.service.WatchListService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.WatchList;

/**
 * Controller for API watchList
 * 
 * @author Grzesiek Bylica grzegorz.bylica@gmail.com
 */
@Controller 
public class WatchListAPI {
	
	private static final Log LOG = LogFactory.getLog(WatchListAPI.class);
	
	@Resource
	private WatchListService watchListService;
	
	@Resource
	private PointValueService pointValueService;
	
	@Resource
	private DataPointService dataPointService;

	@RequestMapping(value = "/api/watchlist/getNames", method = RequestMethod.GET)
	public @ResponseBody String getNames(HttpServletRequest request) {
		LOG.info("/api/watchlist/getNames");
		
		User user = Common.getUser(request);
		
		if (user != null) {
		
			class WatchListJSON implements Serializable{
				private String xid;
				private String name;
				WatchListJSON(String xid,String name) {
					this.setXid(xid);
					this.setName(name);
				}
				public String getXid() {
					return xid;
				}
				public void setXid(String xid) {
					this.xid = xid;
				}
				public String getName() {
					return name;
				}
				public void setName(String name) {
					this.name = name;
				}
			}
			
			int userId = user.getId();
			int profileId = user.getUserProfile();
			List<WatchList> lstWL = watchListService.getWatchLists(userId, profileId);
			List<WatchListJSON> lst = new ArrayList<WatchListJSON>();
			for (WatchList wl:lstWL) {
				WatchListJSON wlJ = new WatchListJSON(wl.getXid(), wl.getName());
				lst.add(wlJ);
			}
			
			String json = null;
			ObjectMapper mapper = new ObjectMapper();
			try {
				json = mapper.writeValueAsString(lst);
			} catch (JsonProcessingException e) {
				LOG.error(e);
			}
			return json;
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param xid
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/api/watchlist/getPoints/{xid}", method = RequestMethod.GET)
	public @ResponseBody String getPoints(@PathVariable("xid") String xid, HttpServletRequest request) {
		
		LOG.info("/api/watchlist/getPoints/{xid} xid:"+xid);
		
		// check may use watch list
		User user = Common.getUser(request);
		
		if (user != null) {
			class PointJSON implements Serializable{
				private String xid;
				private String name;
				PointJSON(String xid,String name) {
					this.setXid(xid);
					this.setName(name);
				}
				public String getXid() {
					return xid;
				}
				public void setXid(String xid) {
					this.xid = xid;
				}
				public String getName() {
					return name;
				}
				public void setName(String name) {
					this.name = name;
				}
			}
			
			WatchList wl = watchListService.getWatchList(xid);
			watchListService.populateWatchlistData(wl);
			List<PointJSON> lst = new ArrayList<PointJSON>();
			
			for (DataPointVO dpvo : wl.getPointList()){
				PointJSON p = new PointJSON(dpvo.getXid(), dpvo.getName());
				lst.add(p);
			}
			
			String json = null;
			ObjectMapper mapper = new ObjectMapper();
			
			try {
				json = mapper.writeValueAsString(lst);
			} catch (JsonProcessingException e) {
				LOG.error(e);
			}
			
			return json;
		} else {
			return null;
		}
	}
	
	@RequestMapping(value = "/api/watchlist/getChartData/{xid}/{fromData}/{toData}", method = RequestMethod.GET)
	public @ResponseBody String getChartData(@PathVariable("xid") String xid, @PathVariable("fromData") Long fromData, @PathVariable("toData") Long toData, HttpServletRequest request) {
		
		LOG.info("/api/watchlist/getChartData/{xid}/{fromData}/{toData} xid:"+xid+" fromData:"+fromData+" toData:"+toData);
		
		User user = Common.getUser(request);
		
		if (user != null) {
			
			class DataChartJSON implements Serializable{
				private String xid;
				private String name;
				private List<ValueToJSON> values;
				DataChartJSON(String xid, String name, List<ValueToJSON> values) {
					this.setXid(xid);
					this.setName(name);
					this.setValues(values);
				}
				public String getXid() {
					return xid;
				}
				public void setXid(String xid) {
					this.xid = xid;
				}
				public String getName() {
					return name;
				}
				public void setName(String name) {
					this.name = name;
				}
				public List<ValueToJSON> getValues() {
					return values;
				}
				public void setValues(List<ValueToJSON> values) {
					this.values=values;
				}
			}
			
			DataPointVO dp = dataPointService.getDataPoint(xid);
			
			List<PointValueTime> listValues = pointValueService.getPointValuesBetween(dp.getId(), fromData, toData);
			
			List<ValueToJSON> values = new ArrayList<ValueToJSON>();
			
			for (PointValueTime pvt : listValues) {
				ValueToJSON v = new ValueToJSON();
				v.set(pvt, dp);
				values.add(v);
			}
			
			DataChartJSON dataChartJSON = new DataChartJSON(xid, dp.getName(), values);
			
			String json = null;
			ObjectMapper mapper = new ObjectMapper();
			
			try {
				json = mapper.writeValueAsString(dataChartJSON);
			} catch (JsonProcessingException e) {
				LOG.error(e);
			}
			return json;
		} else {
			return "";
		}
	}

}
