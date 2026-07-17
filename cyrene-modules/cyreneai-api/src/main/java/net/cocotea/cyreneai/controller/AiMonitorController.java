package net.cocotea.cyreneai.controller;

import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.model.vo.AiCostStatVO;
import net.cocotea.cyreneai.model.vo.AiCostSuggestionVO;
import net.cocotea.cyreneai.model.vo.AiModelRankVO;
import net.cocotea.cyreneai.model.vo.AiMonitorOverviewVO;
import net.cocotea.cyreneai.model.vo.AiTokenTrendVO;
import net.cocotea.cyreneai.model.vo.AiUserRankVO;
import net.cocotea.cyreneai.service.AiMonitorService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;

import java.util.List;

@Controller
@Mapping("/ai/monitor")
@Valid
public class AiMonitorController {

    @Inject
    private AiMonitorService aiMonitorService;

    @Mapping("/overview")
    @Get
    public ApiResult<AiMonitorOverviewVO> overview(@Param(value = "days", required = false) Integer days) {
        return ApiResult.ok(aiMonitorService.overview(days));
    }

    @Mapping("/tokenTrend")
    @Get
    public ApiResult<List<AiTokenTrendVO>> tokenTrend(@Param(value = "groupType", required = false) String groupType,
                                                      @Param(value = "days", required = false) Integer days) {
        return ApiResult.ok(aiMonitorService.tokenTrend(groupType, days));
    }

    @Mapping("/modelRank")
    @Get
    public ApiResult<List<AiModelRankVO>> modelRank(@Param(value = "days", required = false) Integer days) {
        return ApiResult.ok(aiMonitorService.modelRank(days));
    }

    @Mapping("/userRank")
    @Get
    public ApiResult<List<AiUserRankVO>> userRank(@Param(value = "days", required = false) Integer days) {
        return ApiResult.ok(aiMonitorService.userRank(days));
    }

    @Mapping("/costStat")
    @Get
    public ApiResult<List<AiCostStatVO>> costStat(@Param(value = "dimension", required = false) String dimension,
                                                  @Param(value = "days", required = false) Integer days) {
        return ApiResult.ok(aiMonitorService.costStat(dimension, days));
    }

    @Mapping("/costSuggestions")
    @Get
    public ApiResult<List<AiCostSuggestionVO>> costSuggestions(@Param(value = "days", required = false) Integer days) {
        return ApiResult.ok(aiMonitorService.costSuggestions(days));
    }
}
