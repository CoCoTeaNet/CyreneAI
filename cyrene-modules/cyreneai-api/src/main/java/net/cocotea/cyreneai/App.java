package net.cocotea.cyreneai;

import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneadmin.constant.GlobalConst;
import net.cocotea.cyreneadmin.properties.AppSystemProp;
import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.SolonMain;
import org.noear.solon.core.AppContext;

@Slf4j
@SolonMain
@Import(scanPackages = {"net.cocotea.cyreneadmin", "net.cocotea.cyreneai"})
public class App {

    public static void main(String[] args) {
        SolonApp app = Solon.start(App.class, args);
        AppContext context = app.context();

        AppSystemProp appSystemProp = context.getBean(AppSystemProp.class);
        log.warn("强密码：{}, 权限缓存状态：{}", appSystemProp.getStrongPassword(), appSystemProp.getPermissionCache());

        GlobalConst.START_TIME = System.currentTimeMillis();
    }

}
