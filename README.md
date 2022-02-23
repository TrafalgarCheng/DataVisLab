# DataVisLab

## 后台系统效果图：
![b6ab008bc0d94fc5e8e78aa83397d99](https://user-images.githubusercontent.com/52690597/155254754-87c18606-e475-4946-959f-6fa9bfba0b7e.jpg)
![a587d582ccbe4d0e4c50c3f5324acec](https://user-images.githubusercontent.com/52690597/155254778-cb7ef9fd-7916-4eeb-be4a-ab46cd3e1785.jpg)
![5774250fc2e6174d8403bef15775f52](https://user-images.githubusercontent.com/52690597/155254788-dbffe29b-5ae2-4831-aeae-010d34159a6f.jpg)
![a2efd2901f7bc4fc82ea5ef2490d7e7](https://user-images.githubusercontent.com/52690597/155254800-671f725a-54b4-419f-8cf9-19f737e02e20.jpg)

## 后台系统分析
### 1.1 登陆模块：
* 数据库中查询用户，所以存储用户用session.setAttribute。验证用户用session.getAttribute。
* 一般进行加密存储，方式是用token（用户令牌）进行用户状态保持和验证.
* 登录验证流程：
![a525034e1b26357865938eb8e1356c1](https://user-images.githubusercontent.com/52690597/155254453-c1ab8d56-a9fc-4242-97cd-e97a77670947.jpg)
* 建数据库表：id，用户名，md5密码，token，是否删除，添加时间
* Dao：在配置文件中的User mapper中配置具体方法实现，原始dao层只有接口.
* 方法：通过查询用户名，密码和token值返回用户对象.
* 业务层（service）：作为User对象获取dao层数据，判断是否需要更新token，然后返回user对象.
* 控制层（controller）：用StringUtils工具类判断输入的用户名和密码是否为空，再调用业务层返回user对象，最后再返回result类。
* Result类单独说：返回的结果不止包含数据，还有message告诉成功还是失败，一个code200，404来告诉具体状态，用一个const类存不同状态对应的code值，用resultgenerator来按照controller的逻辑修改result的参数（通过setter）, 最后result的值和信息通过@RequestMapping指定的路径在访问这个路径时调用login方法然后显示结果.

* 登录状态保持：后端实现token值是否有效，因为大部分接口都需要验证登陆，所以进行方法抽取，用aop注解切面来返回用户信息（需要自定义注解：1.@interface + 注解名，2. 元注解：修饰注解的注解，@Target：注解用在哪种java元素上， @Retention：注解的生命周期，@Documented），再用自定义方法参数解析器HandlerMethodArgumentResolver取出request header中的user，调用service层方法比较token，返回user对象。 最后在spring-mvc配置文件中配置参数解析器，直接在controller类中需要用这个注解的方法or参数上添加注解就可以自动验证token。

### 1.2 分页功能 
* 后端按照前端的需求将分页所需数据查询出来。后端只需要提供总页数，必不可少的两个参数1 需要的页码 2 每页条数
* 数据交互流程：
	前端将页码和条数两个参数通过 HTTP 请求传输给后端；
	后端获取到这两个参数后进行参数验证，查询后将当前页的所有数据实体和数据总量封装；
	后端将封装数据返回给前端；
	前端获取到数据和数据量后分别对当前页数据进行渲染和展示，同时完成分页信息区的计算和展示。
  之后定义后端数据的响应格式，在Result里，然后定义分页结果集的数据格式。
* DAO层:实现分页功能都需要在对应实体的DAO Mapper的xml文件中添加查询总数目和查询列表的sql语句（其中要包含分页的两个参数：start页数和limit每页条数）。
* Controller层：获得前端的param，验证页数和条数是否为空，最后调用业务层将PageUtil（页数类，用于获取前端发送过来的map数据中的页数和条数）对象传递过去。
* Service层：调用DAO层两个方法，返回PageResult类也就是结果集，其实最后进入了result类和code与message一起到前端。

### 2.1 图片管理模块：
* 主要使用mvc的multipartResolver工具类实现文件上传。如果接受到了文件上传请求，dispatcher调用resolveMultipart方法装饰请求HttpServletRequest并返回multiHttpServletRequest类型（包含文件对象）。
![d2e663092822f3bea9f74bf45419a3a](https://user-images.githubusercontent.com/52690597/155254562-b55523a5-d1f6-479f-9d10-f4b016cbbe98.jpg)
具体流程：首先判断请求对象request，然后对请求头的contentType进行判断。当请求不为空&&contentType不为空&&contentType值以multipart开头，就返回true。 之后dispatcher用resolveMultipart封装request，实际调用CommonsMultipart中的resolveMultipart（），最终是调用fileuploadbase中的parseRequest和CommonsMultipartresolver的parseFileItems方法。 所以我们可以直接使用返回的文件对象，不用对文件进行解析了。

四大功能：列表，添加，编辑，删除

具体实现：导包，mvc配置文件中设置MultipartResolver。 之后在controller层用一个类的一个方法直接使用MultipartFile对象，判断文件类型并加上设置好的前缀存到upload文件目录下
数据库表设计：id，图片路径，备注，是否已删除，添加时间
DAO层：实现增删改查的sql语句， 删除是逻辑删除（用update方法将is_delete改为1）
Service层：增删改查方法：分页查询用pageUtils工具类，List接收找到的图片对象。删除多个图片对象，输入的是id数组
Controller层：增删改逻辑几乎一样，先判断获得的对象是否为空，最后如果service层对应方法返回值》0，就返回result成功，否则失败。中间增：path和备注有一个为空就返回参数异常，改：先检查id，路径，备注是否为空，同上，然后通过id返回对象，对象为空则也返回失败。删，输入的是数组，判断数组长度分情况，长度小于1返回参数错误。 查询，如果查一个图，步骤同上，id《1则错误，没有图则错误。如果查列表，用map存页数和limit，有一个参数为空则错误，最后返回调service层获得的pageUtils对象
所有方法里的User对象加token注解验证。

### 2.2 多图与大文件上传：多图上传后端没有变化，前端执行多次上传操作。上传其他类型的文件只需要修改代码中的文件格式。
大文件上传的难点：服务端对请求大小进行限制，虽然可以修改设置解决，但是依然会造成后端程序卡死，服务器资源紧张等问题。文件上传流程是后端先上传到服务器之前先缓存为临时文件或者缓存到内存中，再调用相关api进行保存，在大文件上传时无法及时作出响应。根本原因是大文件占用内存多，耗时间和资源，服务器效率下降严重。
解决方法：利用多文件上传的思路，将大文件切分成小文件依次上传，全部传到服务器后再进行合并。
具体实现：在Controller层中加入判断分片的方法checkchunk，验证从前端传过来的两个参数chunks（分片总数），chunk（当前分片序号），然后用StringBuilder生成文件存放地址和文件名，用try抓取这一段的错误，有错误就返回失败。在Controller再加入新的upload方法，从request中获取存放地址，同样用Builder组成文件名，如果存放地址没有分片，就新建一个文件夹，如果是最后一个分片，就调用合并方法合并所有文件（具体实现是用FileInputStream拿取每一个分片，再通过SequenceInputStream两个分片文件进行合并）。之后删除原来分片所在文件夹。
断点续传就是再上传前验证分片是否已存在的逻辑。


### 3 富文本信息管理模块：
前端使用KindEditor富文本编辑器
数据库表：id，标题，内容，添加人，创建时间，更新时间，是否已删除
查询一个或者列表形式数据的流程图：
![e3e3eb278c71e6f9e73edb327b35d93](https://user-images.githubusercontent.com/52690597/155254674-4c30268f-59bb-4c3e-8103-64bac2d2e2e5.jpg)
内容添加功能的流程图：
![8ca4997c1a6e0d30834a4c0ea16f90e](https://user-images.githubusercontent.com/52690597/155254690-e89436fc-daa0-4ee7-aff7-d8a1a6afb173.jpg)
内容修改的流程图：
![c6d4ba53ba6b118a10771086574ce55](https://user-images.githubusercontent.com/52690597/155254715-7b08e90d-1659-4a11-92ba-93eac8ac6dc6.jpg)
内容删除的流程图：
![5299fd8a9f12df6bf6123d2920f4cff](https://user-images.githubusercontent.com/52690597/155254722-5048d37d-c5f3-489f-a882-c92542783582.jpg)
DAO层：7个功能：查一个，查多个（分页），增加一个，修改一个，删除一个，删除多个，查文章总数。 删除还是逻辑删除
Service层：对应上边7个功能，再加一个不分页查询多个（输入是map，返回List）
Controller层：基本和图片模块一样。

### 4 搜索功能：
搜索流程：
![d2daced65a4ddf788ddb787e50b1b7f](https://user-images.githubusercontent.com/52690597/155254738-2a1f8b4b-d8a0-48c6-b17a-f0236d220881.jpg)
前端时间框用flatpickr插件
后端：Controller层：添加一个search方法，接收用户输入的keyword（包含内容和时间），最后调用service层获取getarticlepage。
service层：无
DAO层：改变两个查询语句，为什么要用查total（文章总数的功能），因为返回分页工具类需要这个参数。对sql语句中所有and语句前后加上if标签，使用LIKE， %做模糊查询，条件是keyword，time等不为空，不为空格。
